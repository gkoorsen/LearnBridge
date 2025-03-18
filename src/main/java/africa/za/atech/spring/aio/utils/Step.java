package africa.za.atech.spring.aio.utils;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Step {

    private final int id;
    private final LocalDateTime endTime;
    private final String description;
    private final String status;
    private final String data;

    private Step(int id, LocalDateTime endTime, String description, String status, String data) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.data = data;
        this.endTime = endTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String description;
        private String status;
        private String data;

        Builder() {
        }

        public Builder with(int stepNumber, String stepDescription, String stepStatus, String stepData) {
            this.id = stepNumber;
            this.description = stepDescription;
            this.status = stepStatus;
            this.data = stepData;
            return this;
        }

        public Step build() {
            return new Step(this.id, LocalDateTime.now(), this.description, this.status, this.data);
        }
    }
}
