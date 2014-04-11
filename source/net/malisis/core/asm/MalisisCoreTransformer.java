package net.malisis.core.asm;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class MalisisCoreTransformer extends MalisisClassTransformer
{

	@Override
	public void registerHooks()
	{
		register(keyboardEventHook());
	}

	public AsmHook keyboardEventHook()
	{
		AsmHook ah = new AsmHook("net.minecraft.client.Minecraft", "runTick", "()V");
		
		LabelNode falseLabel = new LabelNode();
		//if(MalisisHooks.onKeyPressed())
		InsnList insert1 = new InsnList();
		insert1.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/event/MalisisHooks", "onKeyPressed", "()Z"));
		insert1.add(new JumpInsnNode(IFNE, falseLabel));
		
		//L1844: KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
		InsnList match1 = new InsnList();
		match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKey", "()I"));
	    match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKeyState", "()Z"));
	    match1.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/settings/KeyBinding", "setKeyBindState", "(IZ)V"));
		

	    //L1973: FMLCommonHandler.instance().fireKeyInput()
		InsnList match2 = new InsnList();
	    match2.add(new MethodInsnNode(INVOKESTATIC, "cpw/mods/fml/common/FMLCommonHandler", "instance", "()Lcpw/mods/fml/common/FMLCommonHandler;"));
	    match2.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/common/FMLCommonHandler", "fireKeyInput", "()V"));
	    

		
		ah.jumpAfter(match1).insert(insert1).jumpAfter(match2).insert(falseLabel);
		
		return ah;
			
	}
	

}
