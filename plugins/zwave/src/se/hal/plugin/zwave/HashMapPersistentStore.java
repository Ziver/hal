/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.zwave;

import com.whizzosoftware.wzwave.node.NodeCreationException;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.node.ZWaveNodeFactory;
import com.whizzosoftware.wzwave.persist.PersistenceContext;
import com.whizzosoftware.wzwave.persist.PersistentStore;

import java.util.HashMap;
import java.util.Map;

/**
 * A HashMap implementation of PersistentStore.
 *
 * @author Ziver Koc
 */
public class HashMapPersistentStore implements PersistentStore, PersistenceContext {

    private HashMap<String, HashMap<String,Object>> db;

    public HashMapPersistentStore() {
        db = new HashMap<>();
    }


    @Override
    public ZWaveNode getNode(byte nodeId, NodeListener listener) throws NodeCreationException {
        return ZWaveNodeFactory.createNode(this, nodeId, listener);
    }

    @Override
    public void saveNode(ZWaveNode node) {
        node.save(this);
    }

    @Override
    public void close() { }


    @Override
    public Map<String, Object> getNodeMap(int nodeId) {
        return get("" + nodeId);
    }

    @Override
    public Map<String, Object> getCommandClassMap(int nodeId, int commandClassId) {
        return get("" + nodeId + "." + commandClassId);
    }

    private Map<String, Object> get(String key) {
        if (!db.containsKey(key))
            db.put(key, new HashMap<String, Object>());
        return db.get(key);
    }

}