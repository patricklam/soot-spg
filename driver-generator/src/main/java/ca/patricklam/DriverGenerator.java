package ca.patricklam;

import java.util.*;
import soot.*;
import soot.tagkit.*;
import soot.util.Chain;
import soot.options.Options;

public class DriverGenerator {
    public static void main(String[] argv) {
        String baseDir = "/home/plam/production/21.soot-spg/driver-generator/Benchmarks/microbenchmark/";
        // wjtp: whole Jimple transformation pack
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", DriverTransformer.v()));
        Options.v().set_prepend_classpath(true);
        Options.v().set_verbose(true);
        Options.v().set_whole_program(true);
        Options.v().set_output_format(1);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(".:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:"+baseDir+"mvn_dependencies/mockito-all-1.9.5.jar:"+baseDir+"mvn_dependencies/junit-4.12.jar:"+baseDir+"mvn_dependencies/hamcrest-core-1.3.jar:/usr/share/java/ant.jar");
        Scene.v().addBasicClass("android.os.Handler", SootClass.HIERARCHY);

        List<String> pd = new ArrayList<>();
        pd.add("-process-dir");
        pd.add("Benchmarks/microbenchmark/target/payroll-test-0.0.1-SNAPSHOT-tests.jar");
        pd.add("-process-dir");
        pd.add("Benchmarks/microbenchmark/target/payroll-test-0.0.1-SNAPSHOT.jar");
        soot.Main.main(pd.toArray(new String[0]));
    }
}

class DriverTransformer extends SceneTransformer {
    private final static DriverTransformer instance = new DriverTransformer();

    private DriverTransformer() {
    }

    public static DriverTransformer v() {
        return instance;
    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        // Map<String, List<SootClass>> classes_map = new HashMap<>();
        int classCount = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("class Driver {\n");
        sb.append("    public static void main(String[] argv) {");
        Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
        for (SootClass appClass : appClasses) {
            if (!appClass.isConcrete() || appClass.getName().contains("$") || appClass.isLibraryClass())
                continue;

            boolean hasTests = false;
            for (SootMethod sm : appClass.getMethods()) {
                if (isTestMethod(sm) && !sm.isPrivate()) {
                    hasTests = true;
                }
            }
            if (!hasTests)
                continue;

            String obj_name = String.format("obj%d", classCount);
            sb.append(String.format("\n        %s %s = new %s();\n", appClass.getName(), obj_name, appClass.getName()));
            for (SootMethod sm : appClass.getMethods()) {
                if (isTestMethod(sm) && !sm.isPrivate()) {
                    Chain<Unit> units = sm.getActiveBody().getUnits();
                    sb.append(String.format("        %s.%s(); // %d units\n", obj_name, sm.getName(), units.size()));
                }
            }
            classCount++;
        }
        sb.append("    }\n");
        sb.append("}\n");
        System.out.println(sb);
    }

    boolean isTestMethod(SootMethod sm) {
        if (sm.getName().startsWith("test") && sm.getParameterCount() == 0 && sm.getReturnType().toString().equals("void"))
            return true;

        List<Tag> smTags = sm.getTags();
        VisibilityAnnotationTag tag = (VisibilityAnnotationTag) sm.getTag("VisibilityAnnotationTag");
        if (tag != null) {
            for (AnnotationTag annotation : tag.getAnnotations()) {
                if (annotation.getType().equals("Lorg/junit/Test;")) {
                    return true;
                }
            }
        }
        return false;
    }
}
