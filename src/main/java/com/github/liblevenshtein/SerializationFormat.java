package com.github.liblevenshtein;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported serialization types.
 */
@Getter
@RequiredArgsConstructor
public enum SerializationFormat {

  /** Google Protocol Buffers. */
  PROTOBUF("application/octet-stream"),

  /** Java bytecode. */
  BYTECODE("application/octet-stream"),

  // [WARNING] :: PLAIN_TEXT should come last as its Serializer will attempt
  // to deserialize any file as plain text ...
  // -------------------------------------------------------------------------

  /** Plain text dictionary (newline-delimited terms). */
  PLAIN_TEXT("text/plain");

  /**
   * Expected content-type of the serialization format.  The content type does
   * not have to be the expected type, but it should be.
   */
  private final String contentType;
}
