package com.potatob.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by potatob on 11/22/14.
 */
public class ObjExporterIncomplete {

    public static void export(DestinyModel model, String fileName) throws IOException {
        //builder.append("mtllib ").append(fileName).append(".mtl").append('\n');

        int idx =0;
        int idx2 = 0;
        int offset = 0;
        List<DestinyGeometry> geometry = model.getGeometry();
        for (DestinyGeometry geom : geometry) {
            if (geom.getName().equals("482021716-1"))
                continue;

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
                for (int i = 0; i < indices.length; i++) {
                    indices[i]++;
                }
                List<DestinyMesh.Part> meshParts = mesh.getParts();
                for (DestinyMesh.Part part : meshParts) {
                    if (part.getFlags() == 0 || !part.getLodName().contains("0") || part.getIndexCount() == 330)
                        continue;
                    System.out.println(part.getIndexCount() + " " +part.getTriangleCount());
                    int triangleCount = part.getTriangleCount();
                    int start = part.getIndexStart();
                    int count = part.getIndexCount();
                    if (triangleCount > 0) {
                        triangleCount = 0;
                        hasIndices = true;
                        faces.append("g ").append(++idx).append('\n');
                        int lastIndex = 0, currentIndex = 0;
                        if (part.getPrimitive() == 5){
                            for (int k = 0; k < count; k++) {
                                int index = start + k;
                                if (triangleCount == 0) { // first triangle
                                    /*faces.append("f ").append(indices[index]).append("//").append(indices[index]).append(
                                            ' ')
                                            .append(indices[index + 1]).append("//").append(indices[index + 1]).append(' ')
                                            .append(indices[index + 2]).append("//").append(indices[index + 2]).append(
                                            '\n');*/
                                    lastIndex = indices[index + 1];
                                    currentIndex = indices[index + 2];
                                    k += 2;
                                } else {
                                    if (lastIndex == currentIndex || lastIndex == indices[index] || currentIndex == indices[index]){
                                        lastIndex = currentIndex;
                                        currentIndex = indices[index];
                                        continue;
                                    }
                                    if ((triangleCount % 2) == 1){
                                        faces.append("f ").append(indices[index]).append("/").append(indices[index]).append("/").append(indices[index]).append(' ')
                                                .append(currentIndex).append("/").append(currentIndex).append("/").append(currentIndex).append(' ')
                                                .append(lastIndex).append("/").append(lastIndex).append("/").append(lastIndex).append(
                                                '\n');
                                    } else {
                                        faces.append("f ")
                                                .append(lastIndex).append("/").append(lastIndex).append("/").append(lastIndex).append(' ')
                                                .append(currentIndex).append("/").append(currentIndex).append("/").append(currentIndex).append(' ')
                                                .append(indices[index]).append("/").append(indices[index]).append("/").append(indices[index])
                                                .append('\n');
                                    }
                                    lastIndex = currentIndex;
                                    currentIndex = indices[index];
                                }
                                triangleCount++;
                            }
                        } else if (part.getPrimitive() == 3){
                            for (int k = 0; k < count; k += 3) {
                                faces.append("f ").append(indices[start + k]).append("/").append(indices[start + k]).append("/").append(indices[start + k]).append(' ')
                                        .append(indices[start + k + 1]).append("/").append(indices[start + k + 1]).append("/").append(indices[start + k + 1]).append(' ')
                                        .append(indices[start + k + 2]).append("/").append(indices[start + k + 2]).append("/").append(indices[start + k + 2]).append(
                                        '\n');
                            }
                        }
                    }
                }

                if (hasIndices) {
                    List<VertexBuffer> vertexBuffers = mesh.getVertexBuffers();
                    for (VertexBuffer vertexBuffer : vertexBuffers) {
                        Vector3D[] positions = vertexBuffer.getPositions();
                        if (positions != null) {
                            VertexBuffer.Element element = vertexBuffer.getPositionInfo();
                            if (element.getType() > element.getBaseType())
                                continue;
                            System.out.println("vertices: " + positions.length);
                            //System.out.println(vertexBuffer.getElementCount());
                            //System.out.println(vertexBuffer.getCount() + " " + vertexBuffer.getStride() + " " + vertexBuffer.getBuffer().length + " " + element.getOffset() + " " + indexBuffer.length);
                            for (int i = 0; i < positions.length; i++) {
                                Vector3D v3d = positions[i];
                                vertices.append("v ").append(v3d.getX()).append(' ').append(v3d.getY()).append(' ')
                                        .append(v3d.getZ()).append('\n');
                            }
                            //offset += positions.length;
                        }
                        Vector3D[] vbNormals = vertexBuffer.getNormals();
                        if (vbNormals != null) {
                            VertexBuffer.Element element = vertexBuffer.getNormalsInfo();
                            if (element.getType() > element.getBaseType())
                                continue;
                            System.out.println("normals: " + vbNormals.length);
                            for (int i = 0; i < vbNormals.length; i++) {
                                Vector3D v3d = vbNormals[i];
                                normals.append("vn ").append(v3d.getX()).append(' ').append(v3d.getY()).append(' ')
                                        .append(v3d.getZ()).append('\n');
                            }
                        }
                        Vector2D[] texCoords = vertexBuffer.getTexCoords();
                        if (texCoords != null) {
                            VertexBuffer.Element element = vertexBuffer.getTexCoordsInfo();
                            if (element.getType() > element.getBaseType())
                                continue;
                            for (int i = 0; i < texCoords.length; i++) {
                                Vector2D v3d = texCoords[i];
                                textures.append("vt ").append(v3d.getX()).append(' ').append(v3d.getY()).append('\n');
                            }
                        }
                    }

                    BufferedWriter out = new BufferedWriter(new FileWriter(fileName + (idx2++) + ".obj"));
                    out.write(vertices.append(textures).append(normals).append(faces).toString());
                    out.close();
                }
            }
        }
    }
}
