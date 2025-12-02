package xin.nb1.baseutil.smc.compiler;

import javax.tools.SimpleJavaFileObject;

/**
 * 内存输入Java文件对象
 */
public class MemoryInputJavaFileObject extends SimpleJavaFileObject
{
    private final String code;
    private final String name;

    public MemoryInputJavaFileObject(String name, String code)
    {
        super(java.net.URI.create("string:///" + name), Kind.SOURCE);
        this.name = name;
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return code;
    }

    public String inferBinaryName()
    {
        return name.replace(".java", "");
    }
}
