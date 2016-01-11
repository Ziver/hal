/*
 * Copyright (c) 2015 Ziver
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

package se.hal.page;

import se.hal.intf.HalHttpPage;

import java.util.*;

/**
 * Created by Ziver on 2015-04-02.
 */
public class HalNavigation implements Iterable{
    private static HashMap<String, HalNavigation> navMap = new HashMap<>();

    private final String id;
    private String name;
    private HalNavigation parentNav;
    private ArrayList<HalNavigation> subNav;
    private HalHttpPage resource;


    /**
     * Create a root navigation object
     */
    public HalNavigation() {
        this(null, null);
    }
    /**
     * Create a sub navigation object with no resource
     */
    public HalNavigation(String id, String name) {
        this.id = id;
        this.name = name;
        this.subNav = new ArrayList<>();
        navMap.put(id, this);
    }
    /**
     * Create a sub navigation object
     */
/*    public HalNavigation(HalHttpPage page) {
        this.id = page.getId();
        this.name = page.getName();
        this.subNav = new ArrayList<>();
        this.resource = page;
        navMap.put(id, this);
    }
*/

    @Override
    public Iterator iterator() {
        return subNav.iterator();
    }
    public List<HalNavigation> getSubNavs() {
        return subNav;
    }
    public HalNavigation getSubNav(String id) {
        for(HalNavigation nav : subNav) {
            if(nav.equals(id))
                return nav;
        }
        return null;
    }

    public HalNavigation addSubNav(HalNavigation nav) {
        nav.setParentNav(this);
        subNav.add(nav);
        return nav;
    }


    private void setParentNav(HalNavigation nav){
        this.parentNav = nav;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof String)
            return this.id.equals(o);
        return this == o ||
                (o != null && this.id.equals(((HalNavigation)o).id));
    }

    public List<HalNavigation> getNavBreadcrumb() {
        LinkedList list = new LinkedList();

        HalNavigation current = this;
        while(current != null && id != null){
            list.addFirst(current);
            current = current.parentNav;
        }

        return list;
    }


    public String getURL(){
        return "/" + this.id;
    }


    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public HalNavigation getParent(){
        return parentNav;
    }


    public static HalNavigation getNav(String id){
        return navMap.get(id);
    }
}
