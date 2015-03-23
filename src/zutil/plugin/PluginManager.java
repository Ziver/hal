/*******************************************************************************
 * Copyright (c) 2014 Ziver
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
 ******************************************************************************/

package zutil.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import com.sun.xml.internal.stream.util.ReadOnlyIterator;
import zutil.io.IOUtil;
import zutil.io.file.FileSearch;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;

/**
 * This class will search the file system for files
 * with the name "plugin.json" that defines data 
 * parameters for a single plugin.
 * The class will only load the latest version of the specific plugin.
 * 
 * @author Ziver
 */
public class PluginManager<T> implements Iterable<PluginData>{
	private static Logger log = LogUtil.getLogger();

	private HashMap<String, PluginData> plugins;


	public static <T> PluginManager<T> load(String path){
		return new PluginManager<T>(path);
	}

	public PluginManager(){
		this("./");
	}
	public PluginManager(String path){
		plugins = new HashMap<String, PluginData>();

		FileSearch search = new FileSearch(new File(path));
		search.setRecursive(true);
		search.searchFolders(false);
		search.setFileName("plugin.json");

		log.fine("Searching for plugins...");
		for(FileSearch.FileSearchItem file : search){
			try {
				DataNode node = JSONParser.read(IOUtil.getContentString(file.getInputStream()));
				PluginData plugin = new PluginData(node);

				if (!plugins.containsKey(plugin.getName()) ||
						plugins.get(plugin.getName()).getVersion() < plugin.getVersion()){
					plugins.put(plugin.getName(), plugin);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Iterator<PluginData> iterator() {
		return plugins.values().iterator();
	}

	public <T> Iterator<T> iterator(Class<T> intf) {
		return new PluginInterfaceIterator<T>(plugins.values().iterator(), intf);
	}

	public class PluginInterfaceIterator<T> implements Iterator<T> {
		private Class<T> intf;
		private Iterator<PluginData> it;
		private PluginData next;

		PluginInterfaceIterator(Iterator<PluginData> it, Class<T> intf){
			this.intf = intf;
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			if(next != null)
				return true;
			while(it.hasNext()) {
				next = it.next();
				if(next.contains(intf))
					return true;
			}
			next = null;
			return false;
		}

		@Override
		public T next() {
			if(!hasNext())
				throw new NoSuchElementException();
			return next.getObject(intf);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Iterator is ReadOnly");
		}
	}
}