package tech.sbdevelopment.mapreflectionapi.api;

public abstract class MapWrapper extends AbstractMapWrapper {
    protected ArrayImage content;

    public MapWrapper(ArrayImage image) {
        this.content = image;
    }

    public ArrayImage getContent() {
        return content;
    }

    @Override
    public abstract MapController getController();
}