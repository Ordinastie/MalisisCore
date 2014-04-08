package net.malisis.core;

import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.AsmInsert;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class HookRegistering
{

	public static void register()
	{
		registerKeyboardEventHook();
	}
	
	public static void registerKeyboardEventHook()
	{
		LabelNode falseLabel = new LabelNode();
		//if(MalisisHooks.onKeyPressed())
		InsnList insert1 = new InsnList();
		insert1.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/event/MalisisHooks", "onKeyPressed", "()Z"));
		insert1.add(new JumpInsnNode(IFEQ, falseLabel));
		
		//L1870: Keyboard.getEventKey() == 62
		InsnList match1 = new InsnList();
		match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKey", "()I"));
		match1.add(new IntInsnNode(BIPUSH, 62));		

	    //L1973: FMLCommonHandler.instance().fireKeyInput()
		InsnList match2 = new InsnList();
	    match2.add(new MethodInsnNode(INVOKESTATIC, "cpw/mods/fml/common/FMLCommonHandler", "instance", "()Lcpw/mods/fml/common/FMLCommonHandler;"));
	    match2.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/common/FMLCommonHandler", "fireKeyInput", "()V"));
	    
		AsmHook ah = new AsmHook("net.minecraft.client.Minecraft", "runTick", "()V");
		ah.addInserts(new AsmInsert(insert1).before(match1), new AsmInsert(falseLabel).after(match2));
		ah.register();
		
			
	}
}
