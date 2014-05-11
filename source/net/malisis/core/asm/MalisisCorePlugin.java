package net.malisis.core.asm;

import com.google.common.base.Throwables;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import net.malisis.core.MalisisCore;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

@TransformerExclusions({"net.malisis.core.asm."})
@IFMLLoadingPlugin.SortingIndex(1001)
public class MalisisCorePlugin implements IFMLLoadingPlugin
{

    public static File source;

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {MalisisCoreTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass()
    {
        return "net.malisis.core.MalisisCore";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        source = (File) data.get("coremodLocation");
        if (source == null)
        { // this is usually in a dev env
            try
            {
                source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            }
            catch (URISyntaxException e)
            {
                throw Throwables.propagate(e);
            }
        }
        MalisisCore.isObfEnv = !((boolean) data.get("runtimeDeobfuscationEnabled"));
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

}
