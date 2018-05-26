/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Ziver Koc
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

package zutil.algo.sort.sortable;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SortableComparableArray implements SortableDataList<Comparable>{
    private Comparable[] list;

    public SortableComparableArray(Comparable[] list){
        this.list = list;
    }

    public Comparable get(int i) {
        return list[i];
    }

    public void set(int i, Comparable o){
        list[i] = o;
    }

    public int size() {
        return list.length;
    }

    public void swap(int a, int b) {
        Comparable temp = list[a];
        list[a] = list[b];
        list[b] = temp;
    }

    public int compare(int a, int b) {
        if(list[a].compareTo(list[b]) < 0){
            return -1;
        }
        else if(list[a].compareTo(list[b]) > 0){
            return 1;
        }
        return 0;
    }

    public int compare(int a, Comparable b) {
        if(list[a].compareTo(b) < 0){
            return -1;
        }
        else if(list[a].compareTo(b) > 0){
            return 1;
        }
        return 0;
    }
}
