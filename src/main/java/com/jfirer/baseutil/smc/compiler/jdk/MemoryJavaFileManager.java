package com.jfirer.baseutil.smc.compiler.jdk;

import com.jfirer.baseutil.smc.compiler.MemoryInputJavaFileObject;
import com.jfirer.baseutil.smc.compiler.MemoryOutputJavaFileObject;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory java file manager.
 *
 * @author michael
 */
public class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    // compiled classes in bytes:
    private final Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

    public MemoryJavaFileManager(JavaFileManager fileManager)
    {
        super(fileManager);
    }

    public Map<String, byte[]> getClassBytes()
    {
        return new HashMap<String, byte[]>(this.classBytes);
    }

    public void clear()
    {
        classBytes.clear();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, Kind kind, FileObject sibling) throws IOException
    {
        if (kind == Kind.CLASS)
        {
            return new MemoryOutputJavaFileObject(className, classBytes);
        }
        else
        {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    public JavaFileObject makeStringSource(String name, String code)
    {
        return new MemoryInputJavaFileObject(name, code);
    }
}
