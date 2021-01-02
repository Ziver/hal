/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ziver Koc
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

package se.hal.plugin.zigbee;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.database.ZigBeeNetworkDataStore;
import com.zsmartsystems.zigbee.database.ZigBeeNodeDao;

import java.util.HashSet;
import java.util.Set;

public class ZigBeeDataStore implements ZigBeeNetworkDataStore {

    @Override
    public Set<IeeeAddress> readNetworkNodes() {
        System.out.println("ZigBeeDataStore.readNetworkNodes()");
        return new HashSet<>();
    }

    @Override
    public ZigBeeNodeDao readNode(IeeeAddress address) {
        System.out.println("ZigBeeDataStore.readNetworkNodes(" + address + ")");
        return null;
    }

    @Override
    public void writeNode(ZigBeeNodeDao node) {
        System.out.println("ZigBeeDataStore.writeNode(" + node + ")");
    }

    @Override
    public void removeNode(IeeeAddress address) {
        System.out.println("ZigBeeDataStore.removeNode(" + address + ")");
    }
}
