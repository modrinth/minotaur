package com.modrinth.minotaur.additionalfiles.container;

import com.modrinth.minotaur.additionalfiles.AdditionalFileType;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFile;

import javax.inject.Inject;

/**
 * The root NamedAdditionalFileContainer class
 */
public class NamedAdditionalFileContainer {
	private final NamedDomainObjectContainer<NamedAdditionalFile> additionalFileContainer;
	private final AdditionalFileType additionalFileType;

	/**
	 * Instantiates a new AdditionalFile object.
	 *
	 * @param container      {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
	 * @param additionalFileType {@link AdditionalFileType}
	 */
	@Inject
	protected NamedAdditionalFileContainer(NamedDomainObjectContainer<NamedAdditionalFile> container, AdditionalFileType additionalFileType) {
		this.additionalFileContainer = container;
		this.additionalFileType = additionalFileType;
	}

	/**
	 * Creates a required resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void requiredResourcePack(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.REQUIRED_RESOURCE_PACK, file));
	}

	/**
	 * Creates an optional resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void optionalResourcePack(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.OPTIONAL_RESOURCE_PACK, file));
	}

	/**
	 * Creates a sources JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void sourceJar(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.SOURCES_JAR, file));
	}

	/**
	 * Creates a dev JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void devJar(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.DEV_JAR, file));
	}

	/**
	 * Creates a Javadoc JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void javadocJar(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.JAVADOC_JAR, file));
	}

	/**
	 * Creates a signature AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void signature(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.SIGNATURE, file));
	}

	/**
	 * Creates another AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void other(final RegularFile file) {
		this.additionalFileContainer.add(new NamedAdditionalFile(AdditionalFileType.OTHER, file));
	}

	/**
	 * RequiredResourcePack AdditionalFileType container class
	 */
	public static class RequiredResourcePack extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new required resource pack object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public RequiredResourcePack(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.REQUIRED_RESOURCE_PACK);
		}
	}

	/**
	 * OptionalResourcePack AdditionalFileType container class
	 */
	public static class OptionalResourcePack extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new optional resource pack object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public OptionalResourcePack(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.OPTIONAL_RESOURCE_PACK);
		}
	}

	/**
	 * SourcesJar AdditionalFileType container class
	 */
	public static class SourcesJar extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new sources jar object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public SourcesJar(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.SOURCES_JAR);
		}
	}

	/**
	 * DevJar AdditionalFileType container class
	 */
	public static class DevJar extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new dev jar object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public DevJar(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.DEV_JAR);
		}
	}

	/**
	 * JavadocJar AdditionalFileType container class
	 */
	public static class JavadocJar extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new Javadoc jar object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public JavadocJar(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.JAVADOC_JAR);
		}
	}

	/**
	 * Signature AdditionalFileType container class
	 */
	public static class Signature extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new signature object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public Signature(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.SIGNATURE);
		}
	}

	/**
	 * Other AdditionalFileType container class
	 */
	public static class Other extends NamedAdditionalFileContainer {
		/**
		 * Instantiates a new signature object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedAdditionalFile>}
		 */
		@Inject
		public Other(NamedDomainObjectContainer<NamedAdditionalFile> container) {
			super(container, AdditionalFileType.OTHER);
		}
	}
}
