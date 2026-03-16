package com.iprody.payment.service.app.async;

import java.time.Instant;
import java.util.UUID;

public interface Message {
    UUID getMessageId();
    Instant getOccurredAt();
}
