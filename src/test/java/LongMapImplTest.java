import de.comparus.opensource.longmap.LongMap;
import de.comparus.opensource.longmap.LongMapImpl;

import org.junit.Test;
import static org.junit.Assert.*;

public class LongMapImplTest {
    @Test
    public void testAssertLongMap() {
        
        LongMap<String> map = new LongMapImpl<>();
        assertTrue(map.isEmpty());
        map.put(3L, "java");
        map.put(5L, "c#");
        map.put(2L, "ruby");
        map.put(1L, "nodejs");
        
        LongMap<String> expected = new LongMapImpl<>();
        expected.put(3L, "java");
        expected.put(5L, "c#");
        expected.put(2L, "ruby");
        expected.put(1L, "nodejs");
        
        assertEquals(4, map.size());
        assertArrayEquals(map.keys(), expected.keys());
        assertArrayEquals(map.values(), expected.values());
        map.remove(5L);
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        assertTrue(map.containsValue("nodejs"));
        assertTrue(map.containsKey(2L));
        assertFalse(map.containsKey(32L));
        map.clear();
        assertTrue(map.isEmpty());
        map.put(6L, null);
        assertTrue(map.containsValue(null));
        
    }
    
}
