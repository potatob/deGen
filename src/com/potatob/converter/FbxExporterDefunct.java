package com.potatob.converter;

import java.io.BufferedWriter;
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
public class FbxExporterDefunct {

    public static void export(DestinyModel model) throws IOException {
        System.out.println("Using FBX exporter.");

        StringBuilder builder = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        // String name = System.getProperty("user.name");

        builder.append(
                String.format("; Generated using deGen. reddit.com/u/potatob\n\n" +
                                "FBXHeaderExtension:  {\n" +
                                "    FBXHeaderVersion: 1003\n" +
                                "    FBXVersion: 6100\n" +
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
                                // lol jk no one wants this // (name == null ? "" : "    Creator: \"" + name + "\"\n") +
                                "    OtherFlags:  {\n" +
                                "        FlagPLE: 0\n" +
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
                if (geom.getName().equals("482021716-1")) {
                    continue;
                }

                StringBuilder geometry = new StringBuilder();

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

                    boolean hasIndices = false;

                    int[] indices = mesh.getIndices();

                    Set<Integer> duplicates = new HashSet<Integer>();

                    for (DestinyMesh.Part part : mesh.getParts()) {
                        if (part.getFlags() == 0 || !part.getLodName().contains("0") || part.getIndexCount() == 330) {
                            continue;
                        }
                        int triangleCount = part.getTriangleCount();
                        int start = part.getIndexStart();
                        int count = part.getIndexCount();
                        if (triangleCount > 0 && !duplicates.contains(start << 16 | triangleCount)) {
                            duplicates.add(start << 16 | triangleCount);
                            System.out.printf("Loading mesh part " + part.hasProgram() + ". Triangles: %d, Offset: %d%n", triangleCount, start);
                            triangleCount = 0;
                            hasIndices = true;
                            int lastIndex = 0, currentIndex = 0;
                            if (part.getPrimitive() == 5) {
                                for (int k = 0; k < count; k++) {
                                    int index = start + k;
                                    if (triangleCount == 0) { // first triangle
                                    /*faces.append("f ").append(indices[index]).append("//").append(indices[index])
                                    .append(
                                            ' ')
                                            .append(indices[index + 1]).append("//").append(indices[index + 1])
                                            .append(' ')
                                            .append(indices[index + 2]).append("//").append(indices[index + 2]).append(
                                            '\n');*/
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
                                        } else {
                                            faces.append(',').append(lastIndex).append(',').append(currentIndex)
                                                    .append(',')
                                                    .append(~indices[index]);
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
                                }
                            }
                        }

                        ObjectHolder object = new ObjectHolder();
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

        builder.append(String.format("Definitions:  {\n" +
                "    Version: 100\n" +
                "    Count: %d\n" +
                "    ObjectType: \"Model\" {\n" +
                "        Count: %d\n" +
                "    }\n" +
                "    ObjectType: \"Material\" {\n" +
                "        Count: 1\n" +
                "    }\n" +
                "}\n\n", ready.size() + 1, ready.size()));

        builder.append("Objects:  {\n");
        int idx = 0;

        StringBuilder connections = new StringBuilder();
        StringBuilder relations = new StringBuilder();

        for (ObjectHolder object : ready) {
            StringBuilder footer = new StringBuilder();

            connections.append(String.format("    Connect: \"OO\", \"Model::Obj_%d\", \"Model::Scene\"\n" +
                    "    Connect: \"OO\", \"Material::Mat_1\", \"Model::Obj_%d\"\n", idx, idx));

            relations.append(String.format("    Model: \"Model::Obj_%d\", \"Mesh\" {\n" +
                    "    }\n", idx));

            builder.append(String.format(
                    "    Model: \"Model::Obj_%d\", \"Mesh\" {\n" +
                            "        Version: 232\n" +
                            "        Properties60:  {\n" +
                            "            Property: \"QuaternionInterpolate\", \"bool\", \"\",0\n" +
                            "            Property: \"Visibility\", \"Visibility\", \"A+\",1\n" +
                            "            Property: \"Lcl Translation\", \"Lcl Translation\", \"A+\",0.000000000000000,0.000000000000000,-534.047119140625000\n" +
                            "            Property: \"Lcl Rotation\", \"Lcl Rotation\", \"A+\",0.000009334667643," +
                            "-0.000000000000000,0.000000000000000\n" +
                            "            Property: \"Lcl Scaling\", \"Lcl Scaling\", \"A+\",1.000000000000000," +
                            "1.000000000000000,1.000000000000000\n" +
                            "            Property: \"RotationOffset\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"RotationPivot\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"ScalingOffset\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"ScalingPivot\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"TranslationActive\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMin\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"TranslationMax\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"TranslationMinX\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMinY\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMinZ\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMaxX\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMaxY\", \"bool\", \"\",0\n" +
                            "            Property: \"TranslationMaxZ\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationOrder\", \"enum\", \"\",0\n" +
                            "            Property: \"RotationSpaceForLimitOnly\", \"bool\", \"\",0\n" +
                            "            Property: \"AxisLen\", \"double\", \"\",10\n" +
                            "            Property: \"PreRotation\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"PostRotation\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"RotationActive\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMin\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"RotationMax\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"RotationMinX\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMinY\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMinZ\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMaxX\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMaxY\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationMaxZ\", \"bool\", \"\",0\n" +
                            "            Property: \"RotationStiffnessX\", \"double\", \"\",0\n" +
                            "            Property: \"RotationStiffnessY\", \"double\", \"\",0\n" +
                            "            Property: \"RotationStiffnessZ\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampRangeX\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampRangeY\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampRangeZ\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampRangeX\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampRangeY\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampRangeZ\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampStrengthX\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampStrengthY\", \"double\", \"\",0\n" +
                            "            Property: \"MinDampStrengthZ\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampStrengthX\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampStrengthY\", \"double\", \"\",0\n" +
                            "            Property: \"MaxDampStrengthZ\", \"double\", \"\",0\n" +
                            "            Property: \"PreferedAngleX\", \"double\", \"\",0\n" +
                            "            Property: \"PreferedAngleY\", \"double\", \"\",0\n" +
                            "            Property: \"PreferedAngleZ\", \"double\", \"\",0\n" +
                            "            Property: \"InheritType\", \"enum\", \"\",0\n" +
                            "            Property: \"ScalingActive\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMin\", \"Vector3D\", \"\",1,1,1\n" +
                            "            Property: \"ScalingMax\", \"Vector3D\", \"\",1,1,1\n" +
                            "            Property: \"ScalingMinX\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMinY\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMinZ\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMaxX\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMaxY\", \"bool\", \"\",0\n" +
                            "            Property: \"ScalingMaxZ\", \"bool\", \"\",0\n" +
                            "            Property: \"GeometricTranslation\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"GeometricRotation\", \"Vector3D\", \"\",0,0,0\n" +
                            "            Property: \"GeometricScaling\", \"Vector3D\", \"\",1,1,1\n" +
                            "            Property: \"LookAtProperty\", \"object\", \"\"\n" +
                            "            Property: \"UpVectorProperty\", \"object\", \"\"\n" +
                            "            Property: \"Show\", \"bool\", \"\",1\n" +
                            "            Property: \"NegativePercentShapeSupport\", \"bool\", \"\",1\n" +
                            "            Property: \"DefaultAttributeIndex\", \"int\", \"\",0\n" +
                            "            Property: \"Color\", \"Color\", \"A\",0.8,0.8,0.8\n" +
                            "            Property: \"Size\", \"double\", \"\",100\n" +
                            "            Property: \"Look\", \"enum\", \"\",1\n" +
                            "        }\n" +
                            "        MultiLayer: 0\n" +
                            "        MultiTake: 1\n" +
                            "        Shading: Y\n" +
                            "        Culling: \"CullingOff\"\n" +
                            "        GeometryVersion: 124\n" +
                            "        Vertices: %s\n" +
                            "        PolygonVertexIndex: %s\n",
                    idx++, object.vertices.toString(), object.indices.toString()
                ));

            if (object.normals.length() != 0) {
                builder.append(String.format(
                        "        LayerElementNormal: 0 {\n" +
                                "            Version: 101\n" +
                                "            Name: \"\"\n" +
                                "            MappingInformationType: \"ByPolygonVertex\"\n" +
                                "            ReferenceInformationType: \"Direct\"\n" +
                                "            Normals: %s\n" +
                                "        }\n", object.normals.toString()));
                footer.append(
                        "                LayerElement: {\n" +
                        "                        Type: \"LayerElementNormal\"\n" +
                        "                        TypedIndex: 0\n" +
                        "                }\n");
            }

            if (object.texCoords.length() != 0) {
                builder.append(String.format(
                            "        LayerElementUV: 0 {\n" +
                            "            Version: 101\n" +
                            "            Name: \"\"\n" +
                            "            MappingInformationType: \"ByPolygonVertex\"\n" +
                            "            ReferenceInformationType: \"IndexToDirect\"\n" +
                            "            UV: %s\n" +
                            "        }\n", object.texCoords.toString()));
                footer.append(
                        "                LayerElement: {\n" +
                                "                        Type: \"LayerElementUV\"\n" +
                                "                        TypedIndex: 0\n" +
                                "                }\n");
            }
            
            builder.append("        LayerElementSmoothing: 0 {\n" +
                    "            Version: 102\n" +
                    "            Name: \"\"\n" +
                    "            MappingInformationType: \"ByPolygon\"\n" +
                    "            ReferenceInformationType: \"Direct\"\n" +
                    "            Smoothing: 1\n" +
                    "        }\n");
            builder.append("        LayerElementTexture: 0 {\n" +
                    "            Version: 101\n" +
                    "            Name: \"\"\n" +
                    "            MappingInformationType: \"NoMappingInformation\"\n" +
                    "            ReferenceInformationType: \"IndexToDirect\"\n" +
                    "            BlendMode: \"Translucent\"\n" +
                    "            TextureAlpha: 1\n" +
                    "            TextureId: \n" +
                    "        }\n");
            builder.append("        LayerElementMaterial: 0 {\n" +
                    "            Version: 101\n" +
                    "            Name: \"\"\n" +
                    "            MappingInformationType: \"AllSame\"\n" +
                    "            ReferenceInformationType: \"IndexToDirect\"\n" +
                    "            Materials: 0\n" +
                    "        }\n");

            /*builder.append("        Layer: 0 {\n            Version: 100\n");
            builder.append(footer);
            builder.append("        }\n");
            builder.append("    }\n");*/

            builder.append("        Layer: 0 {\n" +
                    "            Version: 100\n" +
                    "            LayerElement:  {\n" +
                    "                Type: \"LayerElementNormal\"\n" +
                    "                TypedIndex: 0\n" +
                    "            }\n" +
                    "            LayerElement:  {\n" +
                    "                Type: \"LayerElementSmoothing\"\n" +
                    "                TypedIndex: 0\n" +
                    "            }\n" +
                    "            LayerElement:  {\n" +
                    "                Type: \"LayerElementUV\"\n" +
                    "                TypedIndex: 0\n" +
                    "            }\n" +
                    "            LayerElement:  {\n" +
                    "                Type: \"LayerElementTexture\"\n" +
                    "                TypedIndex: 0\n" +
                    "            }\n" +
                    "            LayerElement:  {\n" +
                    "                Type: \"LayerElementMaterial\"\n" +
                    "                TypedIndex: 0\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n");
        }

        relations.append("    Model: \"Model::Producer Perspective\", \"Camera\" {\n" +
                "    }\n");
        relations.append("    Model: \"Model::Camera Switcher\", \"CameraSwitcher\" {\n" +
                "    }");
        relations.append("    Material: \"Material::Mat_1\", \"\" {\n" +
                "    }\n");

        builder.append("    Material: \"Material::Mat_1\", \"\" {\n" +
                "        Version: 102\n" +
                "        ShadingModel: \"lambert\"\n" +
                "        MultiLayer: 0\n" +
                "        Properties60:  {\n" +
                "            Property: \"ShadingModel\", \"KString\", \"\", \"Lambert\"\n" +
                "            Property: \"MultiLayer\", \"bool\", \"\",0\n" +
                "            Property: \"EmissiveColor\", \"ColorRGB\", \"\",0.8000,0.8000,0.8000\n" +
                "            Property: \"EmissiveFactor\", \"double\", \"\",0.0000\n" +
                "            Property: \"AmbientColor\", \"ColorRGB\", \"\",1.0000,1.0000,1.0000\n" +
                "            Property: \"AmbientFactor\", \"double\", \"\",1.0000\n" +
                "            Property: \"DiffuseColor\", \"ColorRGB\", \"\",0.8000,0.8000,0.8000\n" +
                "            Property: \"DiffuseFactor\", \"double\", \"\",0.8000\n" +
                "            Property: \"Bump\", \"Vector3D\", \"\",0,0,0\n" +
                "            Property: \"TransparentColor\", \"ColorRGB\", \"\",1,1,1\n" +
                "            Property: \"TransparencyFactor\", \"double\", \"\",0.0000\n" +
                "            Property: \"SpecularColor\", \"ColorRGB\", \"\",1.0000,1.0000,1.0000\n" +
                "            Property: \"SpecularFactor\", \"double\", \"\",0.5000\n" +
                "            Property: \"ShininessExponent\", \"double\", \"\",12.3\n" +
                "            Property: \"ReflectionColor\", \"ColorRGB\", \"\",0,0,0\n" +
                "            Property: \"ReflectionFactor\", \"double\", \"\",1\n" +
                "            Property: \"Emissive\", \"ColorRGB\", \"\",0,0,0\n" +
                "            Property: \"Ambient\", \"ColorRGB\", \"\",1.0,1.0,1.0\n" +
                "            Property: \"Diffuse\", \"ColorRGB\", \"\",0.8,0.8,0.8\n" +
                "            Property: \"Specular\", \"ColorRGB\", \"\",1.0,1.0,1.0\n" +
                "            Property: \"Shininess\", \"double\", \"\",12.3\n" +
                "            Property: \"Opacity\", \"double\", \"\",1.0\n" +
                "            Property: \"Reflectivity\", \"double\", \"\",0\n" +
                "        }\n" +
                "    }");

        builder.append("}\n\n");

        builder.append("Relations:  {\n").append(relations).append("}\n\n");

        builder.append("Connections:  {\n").append(connections).append(
                "}\n\n" +
                "Takes:  {\n" +
                "    Current: \"\"\n" +
                "}\n\n");

        builder.append("Version5:  {\n" +
                "    AmbientRenderSettings:  {\n" +
                "        Version: 101\n" +
                "        AmbientLightColor: 0.0,0.0,0.0,0\n" +
                "    }\n" +
                "    FogOptions:  {\n" +
                "        FogEnable: 0\n" +
                "        FogMode: 0\n" +
                "        FogDensity: 0.000\n" +
                "        FogStart: 5.000\n" +
                "        FogEnd: 25.000\n" +
                "        FogColor: 0.1,0.1,0.1,1\n" +
                "    }\n" +
                "    Settings:  {\n" +
                "        FrameRate: \"24\"\n" +
                "        TimeFormat: 1\n" +
                "        SnapOnFrames: 0\n" +
                "        ReferenceTimeIndex: -1\n" +
                "        TimeLineStartTime: 0\n" +
                "        TimeLineStopTime: 479181389250\n" +
                "    }\n" +
                "    RendererSetting:  {\n" +
                "        DefaultCamera: \"Producer Perspective\"\n" +
                "        DefaultViewingMode: 0\n" +
                "    }\n" +
                "}");

        //builder.append("    Material: \"material name\", \"\" { }\n" +
        //        "}");

        String itemName = ItemDefinition.nameFromId(model.getId());
        if (itemName == null) {
            itemName = model.getId() + "";
        }
        itemName = Util.properName(itemName) + ".fbx";

        BufferedWriter out = new BufferedWriter(new FileWriter(itemName));
        out.write(builder.toString());
        out.close();

        System.out.println("Saved model: " + itemName);
    }

    private static class ObjectHolder {

        public StringBuilder texCoords;
        public StringBuilder vertices;
        public StringBuilder indices;
        public StringBuilder normals;
    }
}
