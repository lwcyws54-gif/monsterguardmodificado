import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class ModificarMonsterGuard {
public static void main(String[] args) throws Exception {
if (args.length != 2) {
System.out.println("Uso: java ModificarMonsterGuard entrada.jar saida.jar");
return;
}

Path entrada = Paths.get(args[0]);
Path saida = Paths.get(args[1]);

try (JarFile jar = new JarFile(entrada.toFile());
JarOutputStream out = new JarOutputStream(Files.newOutputStream(saida))) {

Enumeration<JarEntry> entries = jar.entries();

while (entries.hasMoreElements()) {
JarEntry entry = entries.nextElement();
byte[] dados;

try (InputStream in = jar.getInputStream(entry)) {
dados = in.readAllBytes();
}

if (entry.getName().equals("dev/monsterguard/Actors.class")) {
dados = modificarActors(dados);
}

JarEntry novo = new JarEntry(entry.getName());
out.putNextEntry(novo);
out.write(dados);
out.closeEntry();
}
}

System.out.println("JAR modificado criado: " + saida);
}

static byte[] modificarActors(byte[] original) {
ClassNode classe = new ClassNode();
new ClassReader(original).accept(classe, 0);

boolean vulnerabilidade = false;
boolean morte = false;

for (MethodNode metodo : classe.methods) {

if (true) {
AbstractInsnNode[] instrucoes = metodo.instructions.toArray();

for (AbstractInsnNode ins : instrucoes) {
if (ins instanceof MethodInsnNode chamada
&& chamada.name.equals("setInvulnerable")
&& chamada.desc.equals("(Z)V")) {

InsnList substituicao = new InsnList();
substituicao.add(new InsnNode(Opcodes.POP));
substituicao.add(new InsnNode(Opcodes.ICONST_0));

metodo.instructions.insertBefore(ins, substituicao);
vulnerabilidade = true;
}
}
}

if (metodo.name.equals("death")
&& metodo.desc.equals("(Lorg/bukkit/event/entity/EntityDeathEvent;)V")) {
InsnList exclusao = new InsnList();

exclusao.add(new VarInsnNode(Opcodes.ALOAD, 0));
exclusao.add(new FieldInsnNode(
Opcodes.GETFIELD,
"dev/monsterguard/Actors",
"app",
"Ldev/monsterguard/MonsterGuardPlugin;"
));
exclusao.add(new VarInsnNode(Opcodes.ALOAD, 2));
exclusao.add(new MethodInsnNode(
Opcodes.INVOKEVIRTUAL,
"dev/monsterguard/MonsterGuardPlugin",
"erase",
"(Ldev/monsterguard/ActorRecord;)V",
false
));

for (AbstractInsnNode ins : metodo.instructions.toArray()) {
if (ins instanceof MethodInsnNode chamada
&& chamada.name.equals("despawn")
&& chamada.owner.equals("dev/monsterguard/Actors")) {
metodo.instructions.insert(ins, exclusao);
break;
}
}{

// A alteração da morte permanente será aplicada
// após confirmar a estrutura do método.
for (AbstractInsnNode ins : metodo.instructions.toArray()) {
if (ins instanceof MethodInsnNode chamada
&& chamada.name.equals("runTaskLater")
&& chamada.owner.equals("org/bukkit/scheduler/BukkitScheduler")) {

morte = true;
}
}
}
}

System.out.println("Invulnerabilidade alterada: " + vulnerabilidade);
System.out.println("Morte permanente alterada: " + morte);

ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
classe.accept(writer);
return writer.toByteArray();
}
}