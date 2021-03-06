/* Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.util;

import groovy.util.BuilderSupport;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONWriter;
import org.codehaus.groovy.grails.web.util.GrailsPrintWriter;

/**
 * <p>JSonBuilder provides support for creating JSON responses</p>
 *
 * <p>If this builder is used in controllers no views should be configured since
 * content will be written to the HttpServletResponse instance. Also no operations
 * should be performed on the response object prior to passing it to this builder.</p>
 *
 * <p>This builder will set the content type of the response to "application/json"</p>
 *
 * <p>Sending a simple key value pair to the client requires this code:</p>
 *
 * <pre>
 * new JsonBuilder(response).json{ message('Hello World') }
 * </pre>
 *
 * <p>will produce output as follows:</p>
 * <pre>{"message":"Hello World"}</pre>
 *
 * @author Michal Klujszo
 * @author Graeme Rocher
 * @since 0.2
 *
 * @deprecated Use {@link grails.web.JSONBuilder} instead
 *
 * Date Created: May, 18, 2006
 */
@Deprecated
public class JSonBuilder extends BuilderSupport {

    private static final String JSON_BUILDER = "JSON Builder: ";
    private static final String TEXT_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";

    private enum Mode {
        ARRAY,
        OBJECT
    }

    private Stack<Mode> stack = new Stack<Mode>();
    private boolean start = true;
    private JSONWriter writer;

    public JSonBuilder(HttpServletResponse response) throws IOException {
        this(response.getWriter());
        if (response.getContentType() == null) {
            response.setContentType(GrailsWebUtil.getContentType(TEXT_JSON,UTF_8));
        }
    }

    public JSonBuilder(JSONWriter _writer) {
        writer = _writer;
    }

    public JSonBuilder(Writer writer) {
        this(new JSONWriter((!(writer instanceof GrailsPrintWriter)) ? new GrailsPrintWriter(writer) : writer));
    }

    @Override
    protected Object createNode(Object name) {
        int retVal = 1;
        try {
            if (start) {
                start = false;
                writeObject();
            }
            else {
                if (getCurrent() == null && stack.peek() == Mode.OBJECT) {
                    throw new IllegalArgumentException(JSON_BUILDER +
                            "only call to [element { }] is allowed when creating array");
                }

                if (stack.peek() == Mode.ARRAY) {
                    writeObject();
                    retVal = 2;
                }
                writer.key(String.valueOf(name)).array();
                stack.push(Mode.ARRAY);
            }
        }
        catch (JSONException e) {
            throw new IllegalArgumentException(JSON_BUILDER + "invalid element");
        }

        return retVal;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Object createNode(Object key, Map valueMap) {
        try {
            if (stack.peek().equals(Mode.OBJECT)) writer.key(String.valueOf(key));
            writer.object();
            for (Iterator iter = valueMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry element = (Map.Entry) iter.next();
                writer.key(String.valueOf(element.getKey())).value(element.getValue());
            }
            writer.endObject();
            return null;
        }
        catch (JSONException e) {
            throw new IllegalArgumentException(JSON_BUILDER + "invalid element");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Object createNode(Object arg0, Map arg1, Object arg2) {
        throw new IllegalArgumentException(JSON_BUILDER + "not implemented");
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Object createNode(Object key, Object value) {
        if (getCurrent() == null && stack.peek().equals(Mode.OBJECT)) {
            throw new IllegalArgumentException(JSON_BUILDER +
                    "only call to [element { }] is allowed when creating array");
        }
        try {
            int retVal = 0;
            if (stack.peek().equals(Mode.ARRAY)) {
                writeObject();
                retVal = 1;
            }
            if (value instanceof Collection) {
                writer.key(String.valueOf(key));
                handleCollectionRecurse((Collection)value);
            }
            else {
                writer.key(String.valueOf(key)).value(value);
            }
            return retVal != 0 ? new Integer(retVal) : null;
        }
        catch (JSONException e) {
            throw new IllegalArgumentException(JSON_BUILDER + "invalid element");
        }
    }

    @SuppressWarnings("rawtypes")
    private void handleCollectionRecurse(Collection c) throws JSONException {
        writer.array();
        for (Iterator i = c.iterator(); i.hasNext();) {
            Object element = i.next();
            if (element instanceof Collection) {
                handleCollectionRecurse((Collection)element);
            }else {
                writer.value(element);
            }
        }
        writer.endArray();
    }

    @Override
    protected void nodeCompleted(Object parent, Object node) {
        Object last = null;

        if (node != null) {
            try {
                int i = ((Integer)node).intValue();
                while (i-- > 0) {
                    last = stack.pop();
                    if (Mode.ARRAY == last) writer.endArray();
                    if (Mode.OBJECT == last) writer.endObject();
                }
            }
            catch (JSONException e) {
                throw new IllegalArgumentException(JSON_BUILDER + "invalid element on the stack");
            }
        }
    }

    @Override
    protected void setParent(Object arg0, Object arg1) {
        /* do nothing */
    }

    private void writeObject() throws JSONException {
        writer.object();
        stack.push(Mode.OBJECT);
    }
}
