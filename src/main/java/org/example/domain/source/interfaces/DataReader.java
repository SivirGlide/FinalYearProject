package org.example.domain.source.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataReader {
    //Interface used for taking
    public Map<String, List<String>> readMap(String source) throws IOException;
}
