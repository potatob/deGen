package com.potatob.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by potatob on 11/22/14.
 */
public class FbxExporter {

    public static void export(DestinyModel model) throws IOException {
        boolean flag = false;

        System.out.println("Using FBX exporter.");

        StringBuilder builder = new StringBuilder();

        Calendar calendar = Calendar.getInstance();

        builder.append(
                String.format("; FBX 7.4.0 project file\n; Generated using deGen. reddit.com/u/potatob\n\n" +
                                "FBXHeaderExtension:  {\n" +
                                "    FBXHeaderVersion: 1003\n" +
                                "    FBXVersion: 7400\n" +
                                "    CreationTimeStamp:  {\n" +
                                "        Version: 1000\n" +
                                "        Year: %d\n" +
                                "        Month: %02d\n" +
                                "        Day: %02d\n" +
                                "        Hour: %02d\n" +
                                "        Minute: %02d\n" +
                                "        Second: %02d\n" +
                                "        Millisecond: %d\n" +
                                "    }\n" +
                                "}\n\n",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND),
                        calendar.get(Calendar.MILLISECOND)
                ));

        List<ObjectHolder> ready = new ArrayList<ObjectHolder>();

        int error = 0;

        for (DestinyGeometry geom : model.getGeometry()) {
            try {
                System.out.println("Exporting geometry: " + geom.getName());
                List<DestinyMesh> meshes = geom.getMeshes();

                for (DestinyMesh mesh : meshes) {
                    if (!mesh.isVisible()) {
                        continue;
                    }
                    StringBuilder vertices = new StringBuilder();
                    StringBuilder textures = new StringBuilder();
                    StringBuilder normals = new StringBuilder();
                    StringBuilder faces = new StringBuilder();

                    ObjectHolder object = new ObjectHolder();
                    boolean hasIndices = false;

                    int[] indices = mesh.getIndices();
                    for (int i = 0; i < indices.length; i++) {
//                        indices[i]++;
                    }

                    Set<Integer> duplicates = new HashSet<Integer>();

                    boolean diffuseTextureOnly = false;

                    for (DestinyMesh.Part part : mesh.getParts()) {
                        long flags = part.getFlags();
                        if (geom.getName().equals("482021716-1") && flags == 0 && part.getIndexCount() == 330) {
                            continue;
                        }
                        if (!part.getLodName().contains("0")) {
                            continue;
                        }
                        if ((flags & 32) == 0){
                            //continue;
                        }
                        if (part.getDiffuseTexture() != null){
                            diffuseTextureOnly = true;
                        } else if (diffuseTextureOnly) {
                            continue;
                        }
                        boolean skipFaces = false;
                        if (part.getDiffuseTexture() != null && part.getDiffuseTexture().valid()) {
                            object.diffuseTexture = part.getDiffuseTexture();
                        } else if (geom.getDiffuseTexture() != null && geom.getDiffuseTexture().valid()) {
                            object.diffuseTexture = geom.getDiffuseTexture();
                        } else {
                            skipFaces = true;
                        }
                        if (part.getNormalTexture() != null && part.getNormalTexture().valid()) {
                            object.normalTexture = part.getNormalTexture();
                        } else if (geom.getDiffuseTexture() != null && geom.getDiffuseTexture().valid()) {
                            object.normalTexture = geom.getNormalTexture();
                        }
                        if (part.getStackTexture() != null && part.getStackTexture().valid()) {
                            object.stackTexture = part.getStackTexture();
                        } else if (geom.getStackTexture() != null && geom.getStackTexture().valid()) {
                            object.stackTexture = geom.getStackTexture();
                        }
                        if (skipFaces) {
                            continue;
                        }
                        if (model.getDyeInfo() != null) {
                            JsonArray dyeInfo = model.getDyeInfo();
                            for (int i = 0; i < dyeInfo.size(); i++) {
                                JsonObject dye = dyeInfo.get(i).getAsJsonObject();
                                if (dye.get("slot_type_index").getAsInt() == part.getDyeIndex()) {
                                    JsonObject material = dye.get("material_properties").getAsJsonObject();
                                    object.color = new float[4];
                                    JsonArray colorArray;
                                    if (part.usePrimaryColor()) {
                                        colorArray = material.get("primary_color").getAsJsonArray();
                                    } else {
                                        colorArray = material.get("secondary_color").getAsJsonArray();
                                    }
                                    for (int j = 0; j < 4; j++) {
                                        object.color[j] = colorArray.get(j).getAsFloat();
                                    }
                                }
                            }
                        }
                        int triangleCount = part.getTriangleCount();
                        int start = part.getIndexStart();
                        int count = part.getIndexCount();
                        if (triangleCount > 0 && !duplicates.contains(start << 16 | triangleCount)) {
                            //duplicates.add(start << 16 | triangleCount);
                            System.out.printf("Loading mesh part " + part.hasProgram() + ". Triangles: %d, Offset: %d%n", triangleCount, start);
                            triangleCount = 0;
                            hasIndices = true;
                            int lastIndex = 0, currentIndex = 0;
                            if (part.getPrimitive() == 5) {
                                for (int k = 0; k < count; k++) {
                                    int index = start + k;
                                    if (triangleCount == 0) { // first triangle
                                        faces.append(indices[index]).append(',').append(indices[index + 1]).append(',')
                                                .append(~indices[index + 2]);
                                        object.numIndices += 3;
                                        lastIndex = indices[index + 1];
                                        currentIndex = indices[index + 2];
                                        k += 2;
                                    } else {
                                        if (lastIndex == currentIndex || lastIndex == indices[index] ||
                                                currentIndex == indices[index]) {
                                            lastIndex = currentIndex;
                                            currentIndex = indices[index];
                                            continue;
                                        }
                                        if ((triangleCount % 2) == 1) {
                                            if (faces.length() != 0) {
                                                faces.append(',');
                                            }
                                            faces.append(indices[index]).append(',').append(currentIndex).append(',')
                                                    .append(~lastIndex);
                                            object.numIndices += 3;
                                        } else {
                                            faces.append(',').append(lastIndex).append(',').append(currentIndex)
                                                    .append(',')
                                                    .append(~indices[index]);
                                            object.numIndices += 3;
                                        }
                                        lastIndex = currentIndex;
                                        currentIndex = indices[index];
                                    }
                                    triangleCount++;
                                }
                            } else if (part.getPrimitive() == 3) {
                                for (int k = 0; k < count; k += 3) {
                                    if (faces.length() != 0) {
                                        faces.append(',');
                                    }
                                    faces.append(indices[start + k]).append(',').append(indices[start + k + 1])
                                            .append(',')
                                            .append(~indices[start + k + 2]);
                                    object.numIndices += 3;
                                }
                            }
                        }
                    }

                    if (hasIndices) {
                        for (VertexBuffer vertexBuffer : mesh.getVertexBuffers()) {
                            Vector3D[] positions = vertexBuffer.getPositions();
                            if (positions != null) {
                                VertexBuffer.Element element = vertexBuffer.getPositionInfo();
                                if (element.getType() > element.getBaseType()) {
                                    continue;
                                }
                                for (int i = 0; i < positions.length; i++) {
                                    Vector3D v3d = positions[i];
                                    if (vertices.length() != 0) {
                                        vertices.append(',');
                                    }
                                    vertices.append(v3d.getX()).append(',').append(v3d.getY()).append(',').append(
                                            v3d.getZ());
                                    object.numVertices += 3;
                                }
                            }
                            Vector3D[] vbNormals = vertexBuffer.getNormals();
                            if (vbNormals != null) {
                                VertexBuffer.Element element = vertexBuffer.getNormalsInfo();
                                if (element.getType() > element.getBaseType()) {
                                    continue;
                                }
                                for (int i = 0; i < vbNormals.length; i++) {
                                    Vector3D v3d = vbNormals[i];
                                    if (normals.length() != 0) {
                                        normals.append(',');
                                    }
                                    normals.append(v3d.getX()).append(',').append(v3d.getY()).append(',')
                                            .append(v3d.getZ());
                                    object.numNormals += 3;
                                }
                            }
                            Vector2D[] texCoords = vertexBuffer.getTexCoords();
                            if (texCoords != null) {
                                VertexBuffer.Element element = vertexBuffer.getTexCoordsInfo();
                                if (element.getType() > element.getBaseType()) {
                                    continue;
                                }
                                for (int i = 0; i < texCoords.length; i++) {
                                    Vector2D v3d = texCoords[i];
                                    if (textures.length() != 0) {
                                        textures.append(',');
                                    }
                                    textures.append(v3d.getX()).append(',').append(v3d.getY());
                                    object.numTexCoords += 2;
                                }
                            }
                        }

                        object.indices = faces;
                        object.vertices = vertices;
                        object.normals = normals;
                        object.texCoords = textures;
                        ready.add(object);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error++;
            }
        }

        if (ready.size() == 0) {
            System.err.println("Couldn't parse buffers!");
            return;
        } else if (error != 0) {
            System.err.println("Encountered error parsing buffers. Attempting to output model.");
        }
        int idx = 2;

        StringBuilder temp = builder;
        builder = new StringBuilder();

        StringBuilder connections = new StringBuilder();
        StringBuilder materials = new StringBuilder();
        StringBuilder textures = new StringBuilder();

        int numModel = 0;
        int numMaterial = 0;
        int numTextures = 0;

        Set<DestinyTexture> usedTextures = new HashSet<DestinyTexture>();

        connections.append("    C: \"OO\",1,0\n");

        for (ObjectHolder object : ready) {
            connections.append(String.format("    C: \"OO\",%d,1\n"
                    + "    C: \"OO\",%d,%1$d\n", idx, idx + 1));


            String normalMap = null;
            if (object.normalTexture != null) {
                normalMap = object.normalTexture.getId();
//                usedTextures.add(object.normalTexture);
//                numTextures++;
            }

            String bump = null;
            if (object.stackTexture != null) {
                bump = object.stackTexture.getId();
//                usedTextures.add(object.stackTexture);
//                numTextures++;
            }

            int modelIdx = idx;

            numModel++;

            builder.append(String.format("    Model: %d, \"Model::\", \"Mesh\" {\n" +
                    "        Version: 232\n" +
                    "        Properties70:  {\n" +
                    "            P: \"ScalingMax\", \"Vector3D\", \"Vector\", \"\",0,0,0\n" +
//                    (normalMap == null ? "" : "            P: \"NormalMap\", \"Enum\", \"A+U\",0, \"" + normalMap + "\"\n") +
//                    (bump == null ? "" : "            P: \"Bump\", \"Enum\", \"A+U\",0, \"textures/" + bump + "\"\n") +
                    "            P: \"DefaultAttributeIndex\", \"int\", \"Integer\", \"\",0\n" +
                    "        }\n" +
                    "        Shading: T\n" +
                    "        Culling: \"CullingOff\"\n" +
                    "    }\n", idx++));

            builder.append(String.format(
                    "    Geometry: %d, \"Geometry::\", \"Mesh\" {\n" +
                            "        Vertices: *%d {\n" +
                            "            a: %s\n" +
                            "        }\n" +
                            "        PolygonVertexIndex: *%d {\n" +
                            "            a: %s\n" +
                            "        }\n" +
                            "        GeometryVersion: 124\n",
                    idx++, object.numVertices, object.vertices.toString(), object.numIndices, object.indices.toString()
                ));

            if (object.color != null) {
                numMaterial++;
                materials.append(String.format("    Material: %d, \"Material::\", \"\" {\n" +
                        "        Version: 102\n" +
                        "        ShadingModel: \"phong\"\n" +
                        "        MultiLayer: 1\n" +
                        "        Properties70:  {\n" +
                        "            P: \"Diffuse\", \"Color\", \"\", \"A\",0.8,0.8,0.8\n" +
                        "            P: \"DiffuseColor\", \"Color\", \"\", \"A\",%s\n" +
                        "        }\n" +
                        "    }\n", idx, object.color[0] + "," + object.color[1] + "," + object.color[2]));
                connections.append(String.format("    C: \"OO\",%d,%d\n", idx++, modelIdx));
            }

            if (object.normals.length() != 0) {
                builder.append(String.format(
                        "        LayerElementNormal: 0 {\n" +
                                "            Version: 101\n" +
                                "            MappingInformationType: \"ByPolygonVertex\"\n" +
                                "            ReferenceInformationType: \"Direct\"\n" +
                                "            Normals: *%d {\n" +
                                "                a: %s\n" +
                                "            }\n" +
                                "        }\n", object.numNormals, object.normals.toString()));
            }

            if (object.texCoords.length() != 0 && false) {
                builder.append(String.format(
                            "        LayerElementUV: 0 {\n" +
                            "            Version: 101\n" +
                            "            Name: \"UVChannel_%d\"\n" +
                            "            MappingInformationType: \"ByPolygonVertex\"\n" +
                            "            ReferenceInformationType: \"Direct\"\n" +
                            "            UV: *%d {\n" +
                            "                a: %s\n" +
                            "            }\n" +
                            "        }\n", modelIdx, object.numTexCoords, object.texCoords.toString()));
                builder.append("\n" +
                        "        LayerElementTexture: 0 {\n" +
                        "            Version: 101\n" +
                        "            Name: \"\"\n" +
                        "            MappingInformationType: \"NoMappingInformation\"\n" +
                        "            ReferenceInformationType: \"IndexToDirect\"\n" +
                        "            BlendMode: \"Translucent\"\n" +
                        "            TextureAlpha: 1\n" +
                        "            TextureId: \n" +
                        "        }\n");
            }

            DestinyTexture[] texarray = new DestinyTexture[] {
                    object.diffuseTexture, object.stackTexture, object.normalTexture
            };

            for (int i = 0; i < texarray.length; i++) {
                DestinyTexture texture = texarray[i];
                if (texture == null)
                    continue;
                usedTextures.add(texture);
                numTextures++;
                textures.append(String.format("    Texture: %d, \"Texture::%1$d\", \"\"TextureImage\"\" {\n" +
                        "        Type: \"TextureImage\"\n" +
                        "        Version: 202\n" +
                        "        TextureName: \"Texture::%1$d\"\n" +
                        "        Properties70:  {\n" +
                        "            Property: \"TextureTypeUse\", \"enum\", \"\",0\n" +
                        "            Property: \"Texture alpha\", \"Number\", \"A+\",1\n" +
                        "            Property: \"CurrentMappingType\", \"enum\", \"\",0\n" +
                        "            Property: \"WrapModeU\", \"enum\", \"\",0\n" +
                        "            Property: \"WrapModeV\", \"enum\", \"\",0\n" +
                        "            Property: \"UVSwap\", \"bool\", \"\",0\n" +
                        "            Property: \"Translation\", \"Vector\", \"A+\",0,0,0\n" +
                        "            Property: \"Rotation\", \"Vector\", \"A+\",0,0,0\n" +
                        "            Property: \"Scaling\", \"Vector\", \"A+\",1,1,1\n" +
                        "            Property: \"TextureRotationPivot\", \"Vector3D\", \"\",0,0,0\n" +
                        "            Property: \"TextureScalingPivot\", \"Vector3D\", \"\",0,0,0\n" +
                        "            Property: \"UseMaterial\", \"bool\", \"\",0\n" +
                        "            Property: \"UseMipMap\", \"bool\", \"\",0\n" +
                        "            Property: \"CurrentTextureBlendMode\", \"enum\", \"\",1\n" +
                        "            Property: \"UVSet\", \"KString\", \"\", \"UVChannel_%d\"\n" +
                        "        }\n" +
                        "        FileName: \"%s\"\n" +
                        "        RelativeFileName: \"%3$s\"\n" +
                        "        ModelUVTranslation: 0,0\n" +
                        "        ModelUVScaling: 1,1\n" +
                        "        Texture_Alpha_Source: \"None\"\n" +
                        "        Cropping: 0,0,0,0\n" +
                        "    }\n", idx, modelIdx, texture.getId()));
                connections.append(String.format("    C: \"OO\",%d,%d\n", idx++, modelIdx));
            }

            builder.append("    }\n");
        }

        temp.append(String.format("Definitions:  {\n" +
                "    Version: 100\n" +
                "    Count: %d\n" +
                "    ObjectType: \"Model\" {\n" +
                "        Count: %d\n" +
                "    }\n" +
                "    ObjectType: \"Geometry\" {\n" +
                "        Count: %d\n" +
                "    }\n" +
                (numTextures == 0 ? "" :
                        "    ObjectType: \"Texture\" {\n" +
                                "        Count: " + numTextures + "\n" +
                                "    }\n") +
                "    ObjectType: \"Material\" {\n" +
                "        Count: %d\n" +
                "    }\n" +
                "}\n\n", numModel * 2 + numMaterial + numTextures + 1, numModel + 1, numModel, numMaterial));

        temp.append("Objects:  {\n");
        temp.append("    Model: 1, \"Model::\", \"Mesh\" {\n" +
                "        Version: 232\n" +
                "        Properties70:  {\n" +
                "            P: \"ScalingMax\", \"Vector3D\", \"Vector\", \"\",0,0,0\n" +
                "            P: \"DefaultAttributeIndex\", \"int\", \"Integer\", \"\",0\n" +
                "        }\n" +
                "        Shading: T\n" +
                "        Culling: \"CullingOff\"\n" +
                "    }\n");
        temp.append(builder);
        temp.append(materials);
        temp.append(textures);
        builder = temp;
        builder.append("}\n\n");

        builder.append("Connections:  {\n").append(connections).append(
                "}\n\n" +
                "Takes:  {\n" +
                "    Current: \"\"\n" +
                "}\n\n");

        String itemName = ItemDefinition.nameFromId(model.getId());
        if (itemName == null) {
            itemName = model.getId() + "";
        }
        itemName = Util.properName(itemName) + ".fbx";

        File outputDir = new File("deGenGen");
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            outputDir.mkdir();
        }

        for (DestinyTexture texture : usedTextures) {
            System.out.println("Saving referenced texture: deGenGen/" + texture.getId());
            try {
                BufferedOutputStream out =
                        new BufferedOutputStream(new FileOutputStream("deGenGen/" + texture.getId()));
                out.write(texture.getData());
                out.close();
            } catch (Exception e) {
                System.err.println("Couldn't save texture, skipping: " + texture.getId());
                e.printStackTrace();
            }
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDir, itemName)));
        out.write(builder.toString());
        out.close();

        System.out.println("Saved model: " + itemName);
        System.out.println();
        Main.success = true;
    }

    private static class ObjectHolder {

        public StringBuilder texCoords;
        public StringBuilder vertices;
        public StringBuilder indices;
        public StringBuilder normals;

        public DestinyTexture diffuseTexture;
        public DestinyTexture normalTexture;
        public DestinyTexture stackTexture;

        public float[] color;

        public int numVertices = 0;
        public int numIndices = 0;
        public int numTexCoords = 0;
        public int numNormals = 0;
    }
}
