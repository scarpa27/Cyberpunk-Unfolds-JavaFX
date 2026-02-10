package hr.tvz.cyberpunkunfolds.docs;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class DocSignatureFormatter {
    private static final Pattern WORD_SPACES = Pattern.compile("\\s+");

    String classSignature(Class<?> c) {
        String mod = classModifierString(c);
        String kind = classKind(c);

        StringBuilder sb = new StringBuilder();
        if (!mod.isBlank()) sb.append(mod).append(' ');
        sb.append(kind).append(' ').append(typeToString(c));

        TypeVariable<?>[] tps = c.getTypeParameters();
        if (tps.length > 0) {
            sb.append('<').append(Arrays.stream(tps).map(TypeVariable::getName).collect(Collectors.joining(", "))).append('>');
        }

        Type superT = c.getGenericSuperclass();
        if (superT != null && !c.isInterface() && superT != Object.class && !c.isEnum() && !c.isRecord()) {
            sb.append(" extends ").append(typeToString(superT));
        }

        Type[] interfaces = c.getGenericInterfaces();
        if (interfaces.length > 0) {
            sb.append(c.isInterface() ? " extends " : " implements ");
            sb.append(Arrays.stream(interfaces).map(this::typeToString).collect(Collectors.joining(", ")));
        }

        return sb.toString();
    }

    String constructorSignature(Class<?> owner, Executable ctor) {
        return owner.getSimpleName() + methodParamsAndExceptions(ctor);
    }

    String methodSignature(java.lang.reflect.Method m) {
        return typeToString(m.getGenericReturnType()) + ' ' +
               m.getName() + methodParamsAndExceptions(m);
    }

    String typeToString(Type t) {
        if (t == null) return "void";
        String s = t.getTypeName();
        return s.replace("java.lang.", "").replace("hr.tvz.cyberpunkunfolds.", "");
    }

    String classKind(Class<?> c) {
        if (c.isAnnotation()) return "@interface";
        if (c.isInterface()) return "interface";
        if (c.isEnum()) return "enum";
        if (c.isRecord()) return "record";
        return "class";
    }

    String visibility(int mods) {
        if (Modifier.isPublic(mods)) return "public";
        if (Modifier.isProtected(mods)) return "protected";
        if (Modifier.isPrivate(mods)) return "private";
        return "package";
    }

    String joinMods(int mods) {
        String s = Modifier.toString(mods);
        if (s == null || s.isBlank()) return "";
        return WORD_SPACES.matcher(s.trim()).replaceAll(" ") + " ";
    }

    private String classModifierString(Class<?> c) {
        String s = Modifier.toString(c.getModifiers());
        if (s == null) return "";

        if (c.isInterface() || c.isAnnotation()) {
            s = removeWord(s, "interface");
            s = removeWord(s, "abstract");
        }
        return WORD_SPACES.matcher(s.trim()).replaceAll(" ");
    }

    private String methodParamsAndExceptions(Executable m) {
        StringBuilder sb = new StringBuilder();

        sb.append('(');
        Type[] params = m.getGenericParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(typeToString(params[i])).append(" p").append(i);
        }
        sb.append(')');

        Type[] ex = m.getGenericExceptionTypes();
        if (ex.length > 0) {
            sb.append(" throws ")
                    .append(Arrays.stream(ex).map(this::typeToString).collect(Collectors.joining(", ")));
        }
        return sb.toString();
    }

    private static String removeWord(String s, String word) {
        return s.replaceAll("\\b" + Pattern.quote(word) + "\\b", " ");
    }
}
