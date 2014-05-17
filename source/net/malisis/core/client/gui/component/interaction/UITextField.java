package net.malisis.core.client.gui.component.interaction;

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.KeyTypedEvent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.renderer.Drawable;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * UITextField
 *
 * @author PaleoCrafter
 */
public class UITextField extends UIComponent
{

    public static final char CHAR_START_OF_HEADING = 1;
    public static final char CHAR_COPY = 3;
    public static final char CHAR_PASTE = 22;
    public static final char CHAR_COPY_AND_EMPTY = 24;
    private static final ResourceLocation TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/textfield.png");
    private final FontRenderer fontRenderer;
    private Drawable background;
    /**
     * Has the current text being edited on the textbox.
     */
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isEnabled, keyTyped will process the keys.
     */
    private boolean focused;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int lineScrollOffset;
    private int cursorPosition;
    /**
     * other selection position, maybe the same as the cursor
     */
    private int selectionEnd;
    private int enabledColor = 0xe0e0e0;
    private int disabledColor = 0x707070;

    public UITextField(FontRenderer fontRenderer, int width, int height)
    {
        this.fontRenderer = fontRenderer;
        this.focused = !canLoseFocus;
        this.setSize(width, height);
        this.setBackground(new DynamicTexture(TEXTURE, 15, 15, this.getWidth(), this.getHeight(), new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5)));
    }

    public void setBackground(Drawable background)
    {
        this.background = background;
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        getContext().register(this);
    }

    @Override
    public void drawBackground(int mouseX, int mouseY)
    {
        if (background != null)
            background.draw(this.getScreenX(), this.getScreenY());
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        int color = this.enabled ? this.enabledColor : this.disabledColor;
        int cursorOff = this.cursorPosition - this.lineScrollOffset;
        int selectionOff = this.selectionEnd - this.lineScrollOffset;
        String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth() - (this.hasBackground() ? 8 : 0));
        boolean cursorInText = cursorOff >= 0 && cursorOff <= s.length();
        boolean drawCursor = this.focused && this.cursorCounter / 6 % 2 == 0 && cursorInText;
        int x = this.hasBackground() ? this.getScreenX() + 4 : this.getScreenX();
        int y = this.hasBackground() ? this.getScreenY() + (this.getHeight() - 8) / 2 : this.getScreenY();
        int textX = x;

        if (selectionOff > s.length())
        {
            selectionOff = s.length();
        }

        if (s.length() > 0)
        {
            String visibleText = cursorInText ? s.substring(0, cursorOff) : s;
            textX = this.fontRenderer.drawStringWithShadow(visibleText, x, y, color);
        }

        boolean useVerticalCursor = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
        int cursorX = textX;

        if (!cursorInText)
        {
            cursorX = cursorOff > 0 ? x + this.getWidth() : x;
        }
        else if (useVerticalCursor)
        {
            cursorX = textX - 1;
            --textX;
        }

        if (s.length() > 0 && cursorInText && cursorOff < s.length())
        {
            this.fontRenderer.drawStringWithShadow(s.substring(cursorOff), textX, y, color);
        }

        if (drawCursor)
        {
            if (useVerticalCursor)
            {
                RenderHelper.drawRectangle(0xd0d0d0, cursorX, y - 1, 0, 1, this.fontRenderer.FONT_HEIGHT + 2);
            }
            else
            {
                this.fontRenderer.drawStringWithShadow("_", cursorX, y, color);
            }
        }

        if (selectionOff != cursorOff)
        {
            int endX = x + this.fontRenderer.getStringWidth(s.substring(0, selectionOff));
            this.drawCursorVertical(cursorX, y - 1, endX - 1, y + 1 + this.fontRenderer.FONT_HEIGHT);
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public void setSize(Size size)
    {
        super.setSize(size);
        if (background != null)
            background.setSize(size);
    }

    /**
     * draws the vertical line cursor in the textbox
     */
    private void drawCursorVertical(int x, int y, int endX, int endY)
    {
        int i1;

        if (x < endX)
        {
            i1 = x;
            x = endX;
            endX = i1;
        }

        if (y < endY)
        {
            i1 = y;
            y = endY;
            endY = i1;
        }

        if (endX > this.getScreenX() + this.getWidth())
        {
            endX = this.getScreenX() + this.getWidth();
        }

        if (x > this.getScreenX() + this.getWidth())
        {
            x = this.getScreenX() + this.getWidth();
        }

        GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        RenderHelper.drawQuad(x, y, zIndex, endX - x, endY - y, 0, 0, 0, 0);
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    public int getMaxStringLength()
    {
        return this.maxStringLength;
    }

    public void setMaxStringLength(int maxLength)
    {
        this.maxStringLength = maxLength;

        if (this.text.length() > maxLength)
        {
            this.text = this.text.substring(0, maxLength);
        }
    }

    /**
     * Increments the cursor counter
     */
    public void increaseCursorPosition()
    {
        ++this.cursorCounter;
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * Sets the text of the textbox
     */
    public void setText(String text)
    {
        if (text.length() > this.maxStringLength)
        {
            this.text = text.substring(0, this.maxStringLength);
        }
        else
        {
            this.text = text;
        }

        this.jumpToEnd();
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText()
    {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    /**
     * replaces selected text, or inserts text at the position on the cursor
     */
    public void writeText(String text)
    {
        String newText = "";
        String filteredText = ChatAllowedCharacters.filerAllowedCharacters(text);
        int cursor = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int selection = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int length = this.maxStringLength - this.text.length() - (cursor - this.selectionEnd);

        if (this.text.length() > 0)
        {
            newText = newText + this.text.substring(0, cursor);
        }

        int l;

        if (length < filteredText.length())
        {
            newText = newText + filteredText.substring(0, length);
            l = length;
        }
        else
        {
            newText = newText + filteredText;
            l = filteredText.length();
        }

        if (this.text.length() > 0 && selection < this.text.length())
        {
            newText = newText + this.text.substring(selection);
        }

        this.text = newText;
        this.moveCursorBy(cursor - this.selectionEnd + l);
    }

    /**
     * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
     * the cursor.
     */
    public void deleteWords(int amount)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                this.deleteFromCursor(this.getNthWordFromCursor(amount) - this.cursorPosition);
            }
        }
    }

    /**
     * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
     */
    public void deleteFromCursor(int amount)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                boolean left = amount < 0;
                int end = left ? this.cursorPosition + amount : this.cursorPosition;
                int start = left ? this.cursorPosition : this.cursorPosition + amount;
                String s = "";

                if (end >= 0)
                {
                    s = this.text.substring(0, end);
                }

                if (start < this.text.length())
                {
                    s = s + this.text.substring(start);
                }

                this.text = s;

                if (left)
                {
                    this.moveCursorBy(amount);
                }
            }
        }
    }

    /**
     * see @getNthNextWordFromPos() params: N, position
     */
    public int getNthWordFromCursor(int n)
    {
        return this.getNthWordFromPos(n, this.getCursorPosition());
    }

    /**
     * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
     */
    public int getNthWordFromPos(int n, int position)
    {
        return this.getNthWord(n, position, true);
    }

    public int getNthWord(int n, int position, boolean countSpaces)
    {
        int pos = position;
        boolean backwards = n < 0;
        int amount = Math.abs(n);

        for (int i = 0; i < amount; ++i)
        {
            if (backwards)
            {
                while (countSpaces && pos > 0 && this.text.charAt(pos - 1) == ' ')
                {
                    --pos;
                }

                while (pos > 0 && this.text.charAt(pos - 1) != ' ')
                {
                    --pos;
                }
            }
            else
            {
                int length = this.text.length();
                pos = this.text.indexOf(' ', pos);

                if (pos == -1)
                {
                    pos = length;
                }
                else
                {
                    while (countSpaces && pos < length && this.text.charAt(pos) == ' ')
                    {
                        ++pos;
                    }
                }
            }
        }

        return pos;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int amount)
    {
        this.setCursorPosition(this.selectionEnd + amount);
    }

    /**
     * sets the cursors position to the beginning
     */
    public void jumpToBegin()
    {
        this.setCursorPosition(0);
    }

    /**
     * sets the cursors position to after the text
     */
    public void jumpToEnd()
    {
        this.setCursorPosition(this.text.length());
    }

    @Subscribe
    public void keyTyped(KeyTypedEvent event)
    {
        if (focused)
        {
            char keyChar = event.getKeyChar();
            int keyCode = event.getKeyCode();
            switch (keyChar)
            {
                case CHAR_START_OF_HEADING:
                    this.jumpToEnd();
                    this.setSelectionPos(0);
                    break;
                case CHAR_COPY:
                    GuiScreen.setClipboardString(this.getSelectedText());
                    break;
                case CHAR_PASTE:
                    if (this.enabled)
                    {
                        if (this.getSelectedText().equals(text))
                            this.setText(GuiScreen.getClipboardString());
                        else
                            this.writeText(GuiScreen.getClipboardString());
                    }
                    break;
                case CHAR_COPY_AND_EMPTY:
                    GuiScreen.setClipboardString(this.getSelectedText());
                    if (this.enabled)
                    {
                        this.writeText("");
                    }
                    break;
                default:
                    switch (keyCode)
                    {
                        case Keyboard.KEY_BACK:
                            if (GuiScreen.isCtrlKeyDown())
                            {
                                if (this.enabled)
                                {
                                    this.deleteWords(-1);
                                }
                            }
                            else if (this.enabled)
                            {
                                this.deleteFromCursor(-1);
                            }
                            break;
                        case Keyboard.KEY_HOME:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                this.setSelectionPos(0);
                            }
                            else
                            {
                                this.jumpToBegin();
                            }
                            break;
                        case Keyboard.KEY_LEFT:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                if (GuiScreen.isCtrlKeyDown())
                                {
                                    this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                                }
                                else
                                {
                                    this.setSelectionPos(this.getSelectionEnd() - 1);
                                }
                            }
                            else if (GuiScreen.isCtrlKeyDown())
                            {
                                this.setCursorPosition(this.getNthWordFromCursor(-1));
                            }
                            else
                            {
                                this.moveCursorBy(-1);
                            }
                            break;
                        case Keyboard.KEY_RIGHT:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                if (GuiScreen.isCtrlKeyDown())
                                {
                                    this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                                }
                                else
                                {
                                    this.setSelectionPos(this.getSelectionEnd() + 1);
                                }
                            }
                            else if (GuiScreen.isCtrlKeyDown())
                            {
                                this.setCursorPosition(this.getNthWordFromCursor(1));
                            }
                            else
                            {
                                this.moveCursorBy(1);
                            }
                            break;
                        case Keyboard.KEY_END:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                this.setSelectionPos(this.text.length());
                            }
                            else
                            {
                                this.jumpToEnd();
                            }
                            break;
                        case Keyboard.KEY_DELETE:
                            if (GuiScreen.isCtrlKeyDown())
                            {
                                if (this.enabled)
                                {
                                    this.deleteWords(1);
                                }
                            }
                            else if (this.enabled)
                            {
                                this.deleteFromCursor(1);
                            }
                            break;
                        default:
                            if (ChatAllowedCharacters.isAllowedCharacter(keyChar))
                            {
                                if (this.enabled)
                                {
                                    this.writeText(Character.toString(keyChar));
                                }
                                break;
                            }
                    }
            }
        }
    }

    /**
     * Args: x, y, buttonClicked
     */
    @Subscribe
    public void mouseClicked(MouseClickedEvent event)
    {
        Point pos = event.getPosition();
        boolean hovered = this.isHovered(pos);

        if (this.canLoseFocus)
        {
            this.setFocused(hovered);
        }

        if (this.focused && event.getButton().isLeft())
        {
            int x = pos.x - this.getScreenX();

            if (this.hasBackground())
            {
                x -= 4;
            }

            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth() - (this.hasBackground() ? 8 : 0));
            this.setCursorPosition(this.fontRenderer.trimStringToWidth(s, x).length() + this.lineScrollOffset);
        }
    }

    public boolean hasBackground()
    {
        return this.background != null;
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition()
    {
        return this.cursorPosition;
    }

    /**
     * sets the position of the cursor to the provided index
     */
    public void setCursorPosition(int position)
    {
        this.cursorPosition = position;
        int length = this.text.length();

        if (this.cursorPosition < 0)
        {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > length)
        {
            this.cursorPosition = length;
        }

        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * Sets the position of the selection anchor (i.e. position the selection was started at)
     */
    public void setSelectionPos(int selectionPos)
    {
        int length = this.text.length();

        if (selectionPos > length)
        {
            selectionPos = length;
        }

        if (selectionPos < 0)
        {
            selectionPos = 0;
        }

        this.selectionEnd = selectionPos;

        if (this.fontRenderer != null)
        {
            if (this.lineScrollOffset > length)
            {
                this.lineScrollOffset = length;
            }

            int width = this.getWidth() - (this.hasBackground() ? 8 : 0);
            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), width);
            int offsetLength = s.length() + this.lineScrollOffset;

            if (selectionPos == this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(this.text, width, true).length();
            }

            if (selectionPos > offsetLength)
            {
                this.lineScrollOffset += selectionPos - offsetLength;
            }
            else if (selectionPos <= this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.lineScrollOffset - selectionPos;
            }

            if (this.lineScrollOffset < 0)
            {
                this.lineScrollOffset = 0;
            }

            if (this.lineScrollOffset > length)
            {
                this.lineScrollOffset = length;
            }
        }
    }

    /**
     * Sets the text colour for this textbox (disabled text will not use this colour)
     */
    public void setTextColor(int color)
    {
        this.enabledColor = color;
    }

    public void setDisabledTextColor(int disabledColor)
    {
        this.disabledColor = disabledColor;
    }

    /**
     * Getter for the focused field
     */
    public boolean isFocused()
    {
        return this.focused;
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean gainFocus)
    {
        if (gainFocus && !this.focused)
        {
            this.cursorCounter = 0;
        }

        this.focused = gainFocus;
    }

    /**
     * the side of the selection that is not the cursor, may be the same as the cursor
     */
    public int getSelectionEnd()
    {
        return this.selectionEnd;
    }

    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    public void setCanLoseFocus(boolean canLose)
    {
        this.canLoseFocus = canLose;
    }

}
