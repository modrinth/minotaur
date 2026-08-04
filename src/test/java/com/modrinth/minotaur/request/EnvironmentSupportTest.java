package com.modrinth.minotaur.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentSupportTest {

	private final Gson gson = new GsonBuilder().create();

	@Test
	public void testFromValidStrings() {
		assertEquals(EnvironmentSupport.REQUIRED, EnvironmentSupport.from("required"));
		assertEquals(EnvironmentSupport.OPTIONAL, EnvironmentSupport.from("OPTIONAL"));
		assertEquals(EnvironmentSupport.UNSUPPORTED, EnvironmentSupport.from("unsupported"));
		assertEquals(EnvironmentSupport.UNKNOWN, EnvironmentSupport.from("unknown"));
	}

	@Test
	public void testFromNull() {
		assertNull(EnvironmentSupport.from(null));
	}

	@Test
	public void testGsonSerialization() {
		assertEquals("\"required\"", gson.toJson(EnvironmentSupport.REQUIRED));
		assertEquals("\"optional\"", gson.toJson(EnvironmentSupport.OPTIONAL));
		assertEquals("\"unsupported\"", gson.toJson(EnvironmentSupport.UNSUPPORTED));
		assertEquals("\"unknown\"", gson.toJson(EnvironmentSupport.UNKNOWN));
	}

	@Test
	public void testGsonDeserialization() {
		assertEquals(EnvironmentSupport.REQUIRED, gson.fromJson("\"required\"", EnvironmentSupport.class));
		assertEquals(EnvironmentSupport.OPTIONAL, gson.fromJson("\"optional\"", EnvironmentSupport.class));
	}
}
