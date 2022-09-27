/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022 inventivetalent / SBDevelopment - All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.sbdevelopment.mapreflectionapi.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.function.BiConsumer;

/**
 * Update checker class
 *
 * @author Stijn [SBDeveloper]
 * @version 2.3 [27-09-2022] - Added Polymart support ; fixed Spigot support
 * @since 05-03-2020
 */
public class UpdateManager {
    private static final String SPIGOT_API = "https://api.spigotmc.org/legacy/update.php?resource=%d";
    private static final String SPIGOT_DOWNLOAD = "https://api.spiget.org/v2/resources/%s/download";

    private static final String POLYMART_API = "https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=%d&key=version";
    private static final String POLYMART_DOWNLOAD = "https://api.polymart.org/v1/requestUpdateURL/?inject_version=%d&resource_id=%d&user_id=%d&nonce=%d&download_agent=%d&download_time=%d&download_token=%s";

    private final Plugin plugin;
    private final Version currentVersion;
    private final CheckType type;

    //Spigot & Polymart
    private final int resourceID;

    //Polymart only
    private int injector_version;
    private int user_id;
    private int nonce;
    private int download_agent;
    private int download_time;
    private String download_token;

    private BiConsumer<VersionResponse, Version> versionResponse;
    private BiConsumer<DownloadResponse, String> downloadResponse;

    /**
     * Construct a new UpdateManager
     *
     * @param plugin The plugin instance
     */
    public UpdateManager(Plugin plugin) {
        this.plugin = plugin;
        this.currentVersion = new Version(plugin.getDescription().getVersion());
        this.type = CheckType.POLYMART_PAID;
        this.resourceID = Integer.parseInt("%%__RESOURCE__%%");
        this.injector_version = Integer.parseInt("%%__INJECT_VER__%%");
        this.user_id = Integer.parseInt("%%__USER__%%");
        this.nonce = Integer.parseInt("%%__NONCE__%%");
        this.download_agent = Integer.parseInt("%%__AGENT__%%");
        this.download_time = Integer.parseInt("%%__TIMESTAMP__%%");
        this.download_token = "%%__VERIFY_TOKEN__%%";
    }

    public UpdateManager(Plugin plugin, int resourceID) {
        this.plugin = plugin;
        this.currentVersion = new Version(plugin.getDescription().getVersion());
        this.type = CheckType.SPIGOT;
        this.resourceID = resourceID;
    }

    /**
     * Handle the response given by check();
     *
     * @param versionResponse The response
     * @return The updatemanager
     */
    public UpdateManager handleResponse(BiConsumer<VersionResponse, Version> versionResponse) {
        this.versionResponse = versionResponse;
        return this;
    }

    public UpdateManager handleDownloadResponse(BiConsumer<DownloadResponse, String> downloadResponse) {
        this.downloadResponse = downloadResponse;
        return this;
    }

    /**
     * Check for a new version
     */
    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                HttpsURLConnection con;
                if (type == CheckType.POLYMART_PAID) {
                    con = (HttpsURLConnection) new URL(String.format(POLYMART_API, this.resourceID)).openConnection();
                } else {
                    con = (HttpsURLConnection) new URL(String.format(SPIGOT_API, this.resourceID)).openConnection();
                }
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "SBDChecker/2.1");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Version onlineVersion = new Version(response.toString());

                VersionResponse verRes = this.currentVersion.check(onlineVersion);

                Bukkit.getScheduler().runTask(this.plugin, () -> this.versionResponse.accept(verRes, onlineVersion));
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(this.plugin, () -> this.versionResponse.accept(VersionResponse.UNAVAILABLE, null));
            }
        });
    }

    public void runUpdate() {
        File pluginFile = getPluginFile(); // /plugins/XXX.jar
        if (pluginFile == null) {
            this.downloadResponse.accept(DownloadResponse.ERROR, null);
            Bukkit.getLogger().info("Pluginfile is null");
            return;
        }
        File updateFolder = Bukkit.getUpdateFolderFile();
        if (!updateFolder.exists()) {
            if (!updateFolder.mkdirs()) {
                this.downloadResponse.accept(DownloadResponse.ERROR, null);
                Bukkit.getLogger().info("Updatefolder doesn't exists, and can't be made");
                return;
            }
        }
        final File updateFile = new File(updateFolder, pluginFile.getName());

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ReadableByteChannel channel;
            try {
                //https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
                HttpsURLConnection connection;
                if (type == CheckType.POLYMART_PAID) {
                    connection = (HttpsURLConnection) new URL(String.format(POLYMART_DOWNLOAD, this.injector_version, this.resourceID, this.user_id, this.nonce, this.download_agent, this.download_time, this.download_token)).openConnection();
                } else {
                    connection = (HttpsURLConnection) new URL(String.format(SPIGOT_DOWNLOAD, this.resourceID)).openConnection();
                }
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                InputStream stream = connection.getInputStream();
                if (connection.getResponseCode() != 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(stream));

                    String inputLine;
                    StringBuilder responsestr = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responsestr.append(inputLine);
                    }
                    in.close();

                    throw new RuntimeException("Download returned status #" + connection.getResponseCode(), new Throwable(responsestr.toString()));
                }

                channel = Channels.newChannel(stream);
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(this.plugin, () -> this.downloadResponse.accept(DownloadResponse.ERROR, null));
                e.printStackTrace();
                return;
            }

            FileChannel fileChannel = null;
            try {
                FileOutputStream fosForDownloadedFile = new FileOutputStream(updateFile);
                fileChannel = fosForDownloadedFile.getChannel();

                fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(this.plugin, () -> this.downloadResponse.accept(DownloadResponse.ERROR, null));
                e.printStackTrace();
                return;
            } finally {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ioe) {
                        System.out.println("Error while closing response body channel");
                    }
                }

                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException ioe) {
                        System.out.println("Error while closing file channel for downloaded file");
                    }
                }
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> this.downloadResponse.accept(DownloadResponse.DONE, updateFile.getPath()));
        });
    }

    private File getPluginFile() {
        if (!(this.plugin instanceof JavaPlugin)) {
            return null;
        }
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(this.plugin);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not get plugin file", e);
        }
    }

    public enum CheckType {
        SPIGOT, POLYMART_PAID
    }

    public enum VersionResponse {
        LATEST, //Latest version
        FOUND_NEW, //Newer available
        THIS_NEWER, //Local version is newer?
        UNAVAILABLE //Error
    }

    public enum DownloadResponse {
        DONE, ERROR, UNAVAILABLE
    }

    public static class Version {

        private final String version;

        public final String get() {
            return this.version;
        }

        private Version(String version) {
            if (version == null)
                throw new IllegalArgumentException("Version can not be null");
            if (!version.matches("[0-9]+(\\.[0-9]+)*"))
                throw new IllegalArgumentException("Invalid version format");
            this.version = version;
        }

        private VersionResponse check(Version that) {
            String[] thisParts = this.get().split("\\.");
            String[] thatParts = that.get().split("\\.");

            int length = Math.max(thisParts.length, thatParts.length);
            for (int i = 0; i < length; i++) {
                int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
                int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
                if (thisPart < thatPart)
                    return VersionResponse.FOUND_NEW;
                if (thisPart > thatPart)
                    return VersionResponse.THIS_NEWER;
            }
            return VersionResponse.LATEST;
        }
    }
}