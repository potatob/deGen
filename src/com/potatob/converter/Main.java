package com.potatob.converter;

import java.io.Console;
import java.io.IOException;

/**
 * Created by potatob on 11/20/14.
 */
public class Main {

    public static final String VERSION = "1.0";

    // DestinyDB model/texture url
    public static final String CONTENT_URL = "http://des.zamimg.com/static/mv/";

    public static boolean success = false;

    public static void loadModel(String modelId, boolean male) {
        DestinyModel model = new DestinyModel(modelId, male);
        if (model.load()) {
            try {
                FbxExporter.export(model);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printUsageExit() {
        System.out.println("Usage:\n  java -jar ddb-converter.jar model_id");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
//            printUsageExit();
        }
        Console console = System.console();
        System.out.println("Destiny model exporter created by potatob (http://reddit.com/u/potatob)");
        System.out.println("Version " + VERSION);
        System.out.println("If this tool breaks feel free to contact me.\n\n");
        while (true) {
            if (success) {
                System.out.print("Enter an item name or id, or type \"exit\": ");
            } else {
                System.out.print("Enter an item name or id: ");
            }
            String result = console == null ? args[0] : Util.properName(System.console().readLine()).replaceAll("_", " ");
            if (result.equals("exit")) {
                System.out.println("Exiting.");
                System.exit(0);
            }
            System.out.println();
            String modelId;
            if (Util.isNumeric(result)) {
                modelId = result;
            } else {
                try {
                    String name = Util.properName(result).replaceAll("_", " ");
                    modelId = ItemDefinition.idFromName(name);
                    System.out.printf("Found item named \"%s\" with id of %s.%n", name, modelId);
                } catch (Exception e) {
                    System.err.println("Couldn't find model with name: " + result);
                    continue;
                }
            }
            System.out.println();
            System.out.print("Output female model (Y/N): ");
            if (args.length >= 2) {
                result = args[1];
            } else {
                result = console == null ? null : Util.properName(System.console().readLine());
            }
            boolean male = true;
            if ("y".equals(result)) {
                male = false;
                System.out.println("Using the female version of the model if it exists.");
            } else {
                System.out.println("Using the default/male version of model.");
            }
            loadModel(modelId, male);
            if (console == null || args.length != 0)
                break;
        }
    }
}
