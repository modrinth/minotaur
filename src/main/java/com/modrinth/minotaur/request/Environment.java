package com.modrinth.minotaur.request;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * The enum representing the environments a version can be used in.
 * See also: <a href="https://modrinth.com/news/article/new-environments/#new-system">The environments release blog</a>
 */
public enum Environment {
	/**
	 * All functionality is performed exclusively on the client side. Should be compatible with vanilla servers.
	 */
	@SerializedName("client_only")
	CLIENT_ONLY,

	/**
	 * All functionality is performed exclusively on the server side. Should be compatible with vanilla clients if only installed on the server. Also works in Singleplayer.
	 */
	@SerializedName("server_only")
	SERVER_ONLY,

	/**
	 * Only runs on a dedicated server, and not in Singleplayer.
	 */
	@SerializedName("dedicated_server_only")
	DEDICATED_SERVER_ONLY,

	/**
	 * Must be installed on both the client and server.
	 */
	@SerializedName("client_and_server")
	CLIENT_AND_SERVER,

	/**
	 * Must be on the server, but can be on the client as well for enhanced functionality
	 */
	@SerializedName("server_only_client_optional")
	SERVER_ONLY_CLIENT_OPTIONAL,

	/**
	 * Must be on the server, but can be on the client as well for enhanced functionality
	 */
	@SerializedName("client_only_server_optional")
	CLIENT_ONLY_SERVER_OPTIONAL,

	/**
	 * Can be installed on just the client or just the server to function, but functionality is enhanced when it is on both.
	 */
	@SerializedName("client_or_server_prefers_both")
	CLIENT_OR_SERVER_PREFERS_BOTH,

	/**
	 * Can be installed on just the client or just the server, and either one would enable full functionality. There would be no reason to install it on both.
	 */
	@SerializedName("client_or_server")
	CLIENT_OR_SERVER,

	/**
	 * Only works in Singleplayer, does not function in a Multiplayer environment.
	 */
	@SerializedName("singleplayer_only")
	SINGLEPLAYER_ONLY,
}
