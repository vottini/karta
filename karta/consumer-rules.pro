# Keep all public API classes and their public/protected members so R8 does not
# rename or strip them when a consuming app enables minification.
-keep public class systems.untangle.karta.** { public protected *; }
-keep public interface systems.untangle.karta.** { *; }

# Enum classes need values() and valueOf() for Kotlin and Compose internals.
-keep public enum systems.untangle.karta.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **;
}

# Preserve Kotlin metadata so the Kotlin reflection API and the compiler plugin
# can inspect the library's types at runtime (needed by Compose tooling).
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
