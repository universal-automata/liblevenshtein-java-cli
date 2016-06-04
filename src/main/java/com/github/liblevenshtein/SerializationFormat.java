package com.github.liblevenshtein;

import com.google.common.net.MediaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported serialization types.
 */
@Getter
@RequiredArgsConstructor
public enum SerializationFormat {

  // [WARNING] :: The ordering of these formats is intentional.  Rearranging
  // them will likely make the adaptive deserialization algorithm fail on
  // certain inputs.
  // -------------------------------------------------------------------------

  /** Java bytecode. */
  BYTECODE(MediaType.OCTET_STREAM.toString()),

  /** Google Protocol Buffers. */
  PROTOBUF(MediaType.OCTET_STREAM.toString()),

  // [WARNING] :: PLAIN_TEXT should come last as its Serializer will attempt
  // to deserialize any file as plain text ...
  // -------------------------------------------------------------------------

  /** Plain text dictionary (newline-delimited terms). */
  PLAIN_TEXT(MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString());

  /**
   * Expected content-type of the serialization format.  The content type does
   * not have to be the expected type, but it should be.
   * -- GETTER --
   * Expected content-type of the serialization format.  The content type does
   * not have to be the expected type, but it should be.
   * @return Expected content-type of the serialization format.
   */
  private final String contentType;
}
