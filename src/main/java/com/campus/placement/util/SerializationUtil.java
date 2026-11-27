package com.campus.placement.util;

import com.campus.placement.model.PlacementSnapshot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Binary Object Serialization/Deserialization utility for backing up and
 * inspecting {@link PlacementSnapshot} archives.
 */
// [Unit 3: Object Serialization/Deserialization]
public final class SerializationUtil {

    private SerializationUtil() {
    }

    /**
     * Serializes a {@link PlacementSnapshot} to the specified file path.
     *
     * @param snapshot   snapshot object graph
     * @param targetFile destination {@code .ser} file
     * @throws IOException if serialization fails
     */
    public static void saveSnapshot(PlacementSnapshot snapshot, Path targetFile) throws IOException {
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetFile.toFile()))) {
            oos.writeObject(snapshot);
        }
    }

    /**
     * Deserializes a {@link PlacementSnapshot} from a {@code .ser} file.
     *
     * @param sourceFile source {@code .ser} file
     * @return restored {@link PlacementSnapshot}
     * @throws IOException            if file read fails
     * @throws ClassNotFoundException if snapshot class definition is incompatible
     */
    public static PlacementSnapshot loadSnapshot(Path sourceFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sourceFile.toFile()))) {
            return (PlacementSnapshot) ois.readObject();
        }
    }
}
