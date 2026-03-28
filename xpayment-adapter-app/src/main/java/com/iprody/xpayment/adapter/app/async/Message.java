package com.iprody.xpayment.adapter.app.async;

import java.time.Instant;
import java.util.UUID;

public interface Message {
    UUID getMessageId();
    Instant getOccurredAt();
}
