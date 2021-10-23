package dev.jianmu.eventbridge.aggregate.event;

/**
 * @class: ConnectionEvent
 * @description: 转发事件
 * @author: Ethan Liu
 * @create: 2021-08-14 17:23
 **/
public class ConnectionEvent extends BaseEvent {
    private String bridgeId;
    private String sourceId;
    private String sourceEventId;
    private String targetId;
    private String payload;

    public String getBridgeId() {
        return bridgeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getPayload() {
        return payload;
    }


    public static final class Builder {
        private String bridgeId;
        private String sourceId;
        private String sourceEventId;
        private String targetId;
        private String payload;

        private Builder() {
        }

        public static Builder aConnectionEvent() {
            return new Builder();
        }

        public Builder bridgeId(String bridgeId) {
            this.bridgeId = bridgeId;
            return this;
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder sourceEventId(String sourceEventId) {
            this.sourceEventId = sourceEventId;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public ConnectionEvent build() {
            ConnectionEvent connectionEvent = new ConnectionEvent();
            connectionEvent.bridgeId = this.bridgeId;
            connectionEvent.sourceId = this.sourceId;
            connectionEvent.sourceEventId = this.sourceEventId;
            connectionEvent.targetId = this.targetId;
            connectionEvent.payload = this.payload;
            return connectionEvent;
        }
    }
}