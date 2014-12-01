package com.potatob.converter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sun.misc.Launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by potatob on 11/23/14.
 */
public class ItemDefinition {

    // I lucked out finding this URL lol.
    public static final String DEFINITION_URL = "http://www.bungie.net/Platform/Destiny/Manifest/inventoryItem/";

    private static final Map<String, String> DEFINITIONS = new HashMap<String, String>();
    private static final Map<String, String> INV_DEFINITIONS = new HashMap<String, String>();

    static {
        try {
            loadDefinitions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String idFromName(String name) {
        return INV_DEFINITIONS.get(name);
    }

    public static String nameFromId(String id) {
        return DEFINITIONS.get(id);
    }

    public static void loadDefinitions() throws IOException {
        InputStream inp = Launcher.class.getResourceAsStream("/defs.txt");
        if (inp != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(inp));
            String line;
            while ((line = in.readLine()) != null) {
                int separator = line.indexOf("=");
                String id = line.substring(0, separator).trim();
                String name = Util.properName(line.substring(separator + 1).trim()).replaceAll("_", " ");
                DEFINITIONS.put(id, name);
                INV_DEFINITIONS.put(name, id);
            }
            in.close();
        }
        File file = new File("defs.txt");
        if (file.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            while ((line = in.readLine()) != null) {
                int separator = line.indexOf("=");
                String id = line.substring(0, separator).trim();
                String name = Util.properName(line.substring(separator + 1).trim()).replaceAll("_", " ");
                DEFINITIONS.put(id, name);
                INV_DEFINITIONS.put(name, id);
            }
            in.close();
        }
        System.out.println("Loaded " + DEFINITIONS.size() + " item definitions.");
        if (DEFINITIONS.size() == 0) {
            System.out.println("Updating definitions...");
            try {
                //updateDefinitions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateDefinitions() throws ExecutionException, InterruptedException, IOException {
        System.out.println("Attempting to update item definitions.");
        String page = new String(AsyncHttp.get("geom/mobile/").get(), Charset.forName("UTF-8"));
        Pattern r = Pattern.compile("\\d+(?=.geom)");
        Matcher m = r.matcher(page);
        page = null;
        Set<String> ids = new HashSet<String>();
        while (m.find()) {
            String id = m.group();
            if (!DEFINITIONS.containsKey(id)) {
                ids.add(m.group());
            }
        }
        Map<Future<byte[]>, String> pending = new HashMap<Future<byte[]>, String>();
        for (String id : ids) {
            pending.put(AsyncHttp.completionServiceGetWithFullUrl(DEFINITION_URL + id), id);
        }
        CompletionService<byte[]> completionService = AsyncHttp.getCompletionService();
        JsonParser parser = new JsonParser();
        int successCount = 0, failCount = 0;
        for (int i = 0; i < ids.size(); i++) {
            Future<byte[]> response = completionService.take();
            String id = pending.remove(response);
            JsonObject json = (JsonObject) parser.parse(new String(response.get(), Charset.forName("UTF-8")));
            JsonObject inventoryItem = json.getAsJsonObject("Response")
                    .getAsJsonObject("data")
                    .getAsJsonObject("inventoryItem");
            if (inventoryItem.has("itemName")) {
                String name = inventoryItem.get("itemName").getAsString().trim();
                DEFINITIONS.put(id, name);
                System.out.println("Found name for item " + id + ": " + name);
                successCount++;
            } else {
                System.out.println("Couldn't find name for item: " + id);
                failCount++;
            }
        }
        pending = null;
        BufferedWriter out = new BufferedWriter(new FileWriter("META-INF/defs.txt"));
        for (Map.Entry<String, String> def : DEFINITIONS.entrySet()) {
            out.write(def.getKey() + "=" + def.getValue() + "\n");
        }
        out.close();
        System.out.printf("Finished udating definitions. Found %d new definitions. Failed to find %d definitions.",
                successCount, failCount);
    }
}
