/*
 * Copyright 2014 Higher Frequency Trading
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.collections;

import net.openhft.lang.io.serialization.JDKObjectSerializer;
import org.junit.Test;

import java.io.*;
import java.util.Map;

import static org.junit.Assert.assertSame;

public class RecursiveReferenceSHMTest {
    public static class StupidCycle implements Serializable {
        int dummy;
        Object cycle[] = {this};
    }

    public static class StupidCycle2 extends StupidCycle implements Externalizable {
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(dummy);
            out.writeObject(cycle);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            dummy = in.readInt();
            cycle = (Object[]) in.readObject();
        }
    }

    public static final String TMP = System.getProperty("java.io.tmpdir");

    @Test
    public void testRecursive() throws IOException {
        File file = new File(TMP + "/test." + System.nanoTime() + ".tmp");
        file.deleteOnExit();
        Map<String, StupidCycle> map = new SharedHashMapBuilder()
                .entries(64)
                .objectSerializer(JDKObjectSerializer.INSTANCE).file(file).kClass(String.class).vClass(StupidCycle.class).create();

        map.put("Test", new StupidCycle());
        map.put("Test2", new StupidCycle2());
        StupidCycle cycle = (StupidCycle) map.get("Test");
        assertSame(cycle, cycle.cycle[0]);
        StupidCycle cycle2 = (StupidCycle) map.get("Test2");
        assertSame(cycle2, cycle2.cycle[0]);
    }
}
