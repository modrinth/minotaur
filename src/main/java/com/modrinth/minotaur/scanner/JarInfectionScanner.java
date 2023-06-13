package com.modrinth.minotaur.scanner;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import static org.objectweb.asm.Opcodes.*;

/**
 * Jar Scanner to scan for the Fractureiser malware
 * Contains code copied from <a href="https://github.com/MCRcortex/nekodetector/blob/master/src/main/java/me/cortex/jarscanner/Detector.java">...</a>
 * with permission
 */
public class JarInfectionScanner {

	public static void scan(Logger logger, ZipFile file) {
		try {
			boolean matches = file.stream()
				.filter(entry -> entry.getName().endsWith(".class"))
				.anyMatch(entry -> {
					try {
						return scanClass(getByteArray(file.getInputStream(entry)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!matches)
				return;
			throw new GradleException(String.format("!!!! %s is infected with Fractureiser", file.getName()));
		} catch (Exception e) {
			logger.error("Failed to scan {}", file.getName(), e);
		}

		logger.info("Fractureiser not detected in {}", file.getName());
	}

	private static byte[] getByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		return buffer.toByteArray();
	}

	private static final AbstractInsnNode[] SIG1 = new AbstractInsnNode[] {
		new TypeInsnNode(NEW, "java/lang/String"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new TypeInsnNode(NEW, "java/lang/String"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getConstructor",
			"([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKESPECIAL, "java/net/URL", "<init>",
			"(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
			"([Ljava/lang/Object;)Ljava/lang/Object;"),
		new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName",
			"(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
			"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
			"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"),
	};

	private static final AbstractInsnNode[] SIG2 = new AbstractInsnNode[] {
		new MethodInsnNode(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
		new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "concat",
			"(Ljava/lang/String;)Ljava/lang/String;"), // TODO:FIXME: this might not be in all of them
		new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
		new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
		new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
	};

	// The IP
	private static final AbstractInsnNode[] SIG3 = new AbstractInsnNode[] {
		new IntInsnNode(BIPUSH, 56),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new InsnNode(ICONST_1),
		new IntInsnNode(BIPUSH, 53),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new InsnNode(ICONST_2),
		new IntInsnNode(BIPUSH, 46),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new InsnNode(ICONST_3),
		new IntInsnNode(BIPUSH, 50),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new InsnNode(ICONST_4),
		new IntInsnNode(BIPUSH, 49),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new InsnNode(ICONST_5),
		new IntInsnNode(BIPUSH, 55),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 6),
		new IntInsnNode(BIPUSH, 46),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 7),
		new IntInsnNode(BIPUSH, 49),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 8),
		new IntInsnNode(BIPUSH, 52),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 9),
		new IntInsnNode(BIPUSH, 52),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 10),
		new IntInsnNode(BIPUSH, 46),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 11),
		new IntInsnNode(BIPUSH, 49),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 12),
		new IntInsnNode(BIPUSH, 51),
		new InsnNode(BASTORE),
		new InsnNode(DUP),
		new IntInsnNode(BIPUSH, 13),
		new IntInsnNode(BIPUSH, 48)
	};

	private static boolean same(AbstractInsnNode a, AbstractInsnNode b) {
		if (a instanceof TypeInsnNode) {
			TypeInsnNode aa = (TypeInsnNode) a;
			return aa.desc.equals(((TypeInsnNode) b).desc);
		}
		if (a instanceof MethodInsnNode) {
			MethodInsnNode aa = (MethodInsnNode) a;
			return aa.owner.equals(((MethodInsnNode) b).owner)
				&& aa.name.equals(((MethodInsnNode) b).name)
				&& aa.desc.equals(((MethodInsnNode) b).desc);
		}
		if (a instanceof InsnNode) {
			return true;
		}
		throw new IllegalArgumentException("TYPE NOT ADDED");
	}

	public static boolean scanClass(byte[] clazz) {
		ClassReader reader = new ClassReader(clazz);
		ClassNode node = new ClassNode();
		try {
			reader.accept(node, 0);
		} catch (Exception e) {
			return false;// Yes this is very hacky but should never happen with valid clasees
		}
		for (MethodNode method : node.methods) {
			{
				// Method 1, this is a hard detect, if it matches this it is 100% chance
				// infected
				boolean match = true;
				int j = 0;
				for (int i = 0; i < method.instructions.size() && j < SIG1.length; i++) {
					AbstractInsnNode insn = method.instructions.get(i);
					if (insn.getOpcode() == -1) {
						continue;
					}
					if (insn.getOpcode() == SIG1[j].getOpcode()) {
						if (!same(insn, SIG1[j++])) {
							match = false;
							break;
						}
					}
				}
				if (j != SIG1.length) {
					match = false;
				}
				if (match) {
					return true;
				}
			}

			{
				// Method 2, this is a near hard detect, if it matches this it is 95% chance
				// infected
				boolean match = false;
				outer: for (int q = 0; q < method.instructions.size(); q++) {
					int j = 0;
					for (int i = q; i < method.instructions.size() && j < SIG2.length; i++) {
						AbstractInsnNode insn = method.instructions.get(i);
						if (insn.getOpcode() != SIG2[j].getOpcode()) {
							continue;
						}

						if (insn.getOpcode() == SIG2[j].getOpcode()) {
							if (!same(insn, SIG2[j++])) {
								continue outer;
							}
						}
					}
					if (j == SIG2.length) {
						match = true;
						break;
					}
				}
				if (match) {
					return true;
				}
			}

			// Method 3, this looks for a byte array with the IP. This is a likely match.
			{
				boolean match = false;
				// where we're looking in the SIG3 array
				int pos = 0;
				for (int i = 0; i < method.instructions.size(); i++) {
					if (pos == SIG3.length) {
						break;
					}
					AbstractInsnNode insn = method.instructions.get(i);
					if (insn.getOpcode() == -1) {
						continue;
					}
					if (insn.getOpcode() == SIG3[pos].getOpcode()) {
						// the opcode matches

						if (SIG3[pos].getType() == AbstractInsnNode.INT_INSN) {
							// check if operand matches
							IntInsnNode iInsn = (IntInsnNode) insn;
							IntInsnNode sigInsn = (IntInsnNode) SIG3[pos];
							if (iInsn.operand == sigInsn.operand) {
								// operands match
								match = true;
								pos++;
							}
						} else {
							// this is a regular InsnNode; just match
							match = true;
							pos++;
						}
					} else {
						match = false;
						pos = 0;
					}
				}

				if (match) {
					return true;
				}
			}
		}
		return false;
	}
}
