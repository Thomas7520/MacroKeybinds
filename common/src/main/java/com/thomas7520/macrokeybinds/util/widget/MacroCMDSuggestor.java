package com.thomas7520.macrokeybinds.util.widget;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MacroCMDSuggestor {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
    final Minecraft client;
    private final Screen owner;
    final EditBox textField;
    final Font textRenderer;
    private final boolean slashOptional;
    private final boolean suggestingWhenEmpty;
    final int maxSuggestionSize;
    final boolean suggestionsAbove;
    final int color;
    private final List<FormattedCharSequence> messages = Lists.newArrayList();
    private int x;
    private int width;
    @Nullable
    private ParseResults<SharedSuggestionProvider> parse;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Nullable
    private MacroCMDSuggestor.SuggestionWindow window;
    private boolean windowActive;
    boolean completingSuggestions;
    private boolean canLeave = true;

    public MacroCMDSuggestor(Minecraft client, Screen owner, EditBox textField, Font textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int maxSuggestionSize, boolean suggestionsAbove, int color) {
        this.client = client;
        this.owner = owner;
        this.textField = textField;
        this.textRenderer = textRenderer;
        this.slashOptional = slashOptional;
        this.suggestingWhenEmpty = suggestingWhenEmpty;
        this.maxSuggestionSize = maxSuggestionSize;
        this.suggestionsAbove = suggestionsAbove;
        this.color = color;
        textField.setFormatter(this::provideRenderText);
    }

    public void setWindowActive(boolean windowActive) {
        this.windowActive = windowActive;
        if (!windowActive) {
            this.window = null;
        }
    }

    public void setCanLeave(boolean canLeave) {
        this.canLeave = canLeave;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = this.window != null;
        if (bl && this.window.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (!this.textField.isFocused() || keyCode != GLFW.GLFW_KEY_TAB || this.canLeave && !bl) {
            return false;
        } else {
            this.show(true);
            return true;
        }
    }

    public boolean mouseScrolled(double amount) {
        return this.window != null && this.window.mouseScrolled(Mth.clamp(amount, -1.0, 1.0));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.window != null && this.window.mouseClicked((int)mouseX, (int)mouseY, button);
    }

    public void show(boolean narrateFirstSuggestion) {
        Suggestions suggestions;
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone() && !(suggestions = this.pendingSuggestions.join()).isEmpty()) {
            int i = 0;
            for (Suggestion suggestion : suggestions.getList()) {
                i = Math.max(i, this.textRenderer.width(suggestion.getText()));
            }
            int j = Mth.clamp(this.textField.getScreenX(suggestions.getRange().getStart()), 0, this.textField.getScreenX(0) + this.textField.getInnerWidth() - i);
            this.window = new MacroCMDSuggestor.SuggestionWindow(j, textField.getY(), i, this.sortSuggestions(suggestions), narrateFirstSuggestion);
        }
    }

    public boolean isOpen() {
        return this.window != null;
    }

    public Component getSuggestionUsageNarrationText() {
        if (this.window != null && this.window.completed) {
            if (this.canLeave) {
                return Component.translatable("narration.suggestion.usage.cycle.hidable");
            }
            return Component.translatable("narration.suggestion.usage.cycle.fixed");
        }
        if (this.canLeave) {
            return Component.translatable("narration.suggestion.usage.fill.hidable");
        }
        return Component.translatable("narration.suggestion.usage.fill.fixed");
    }

    public void clearWindow() {
        this.window = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        String string = this.textField.getValue().substring(0, this.textField.getCursorPosition());
        int i = MacroCMDSuggestor.getStartOfCurrentWord(string);
        String string2 = string.substring(i).toLowerCase(Locale.ROOT);
        ArrayList<Suggestion> list = Lists.newArrayList();
        ArrayList<Suggestion> list2 = Lists.newArrayList();
        for (Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getText().startsWith(string2) || suggestion.getText().startsWith("minecraft:" + string2)) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        return list;
    }

    public void refresh() {
        ClientPacketListener connection = this.client.getConnection();
        if(connection == null) {
            this.parse = null;
            this.pendingSuggestions = null;
            this.window = null;
            this.messages.clear();
            this.textField.setSuggestion(null);
            return;
        }

        String string = this.textField.getValue();
        if (this.parse != null && !this.parse.getReader().getString().equals(string)) {
            this.parse = null;
        }

        if (!this.completingSuggestions) {
            this.textField.setSuggestion((String)null);
            this.window = null;
        }

        this.messages.clear();
        StringReader stringReader = new StringReader(string);
        boolean bl = stringReader.canRead() && stringReader.peek() == '/';
        if (bl) {
            stringReader.skip();
        }

        boolean bl2 = this.slashOptional || bl;
        int i = this.textField.getCursorPosition();
        int j;
        if (bl2) {
            CommandDispatcher<SharedSuggestionProvider> commandDispatcher = connection.getCommands();
            if (this.parse == null) {
                this.parse = commandDispatcher.parse(stringReader, connection.getSuggestionsProvider());
            }

            j = this.suggestingWhenEmpty ? stringReader.getCursor() : 1;
            if (i >= j && (this.window == null || !this.completingSuggestions)) {
                CompletableFuture<Suggestions> suggestions = commandDispatcher.getCompletionSuggestions(this.parse, i);
                this.pendingSuggestions = suggestions;
                suggestions.thenRun(() -> {
                    if (this.pendingSuggestions == suggestions) {
                        this.showCommandSuggestions();
                    }
                });
            }
        } else {
            String string2 = string.substring(0, i);
            j = getStartOfCurrentWord(string2);
            Collection<String> collection = connection.getSuggestionsProvider().getCustomTabSugggestions();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(string2, j));
        }

    }

    private static int getStartOfCurrentWord(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = WHITESPACE_PATTERN.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }

    private static FormattedCharSequence formatException(CommandSyntaxException exception) {
        Component text = ComponentUtils.fromMessage(exception.getRawMessage());
        String string = exception.getContext();
        if (string == null) {
            return text.getVisualOrderText();
        }
        return Component.translatable("command.context.parse_error", text, exception.getCursor(), string).getVisualOrderText();
    }

    private void showCommandSuggestions() {
        boolean bl = false;
        if (this.textField.getCursorPosition() == this.textField.getValue().length()) {
            if (((Suggestions)this.pendingSuggestions.join()).isEmpty() && !this.parse.getExceptions().isEmpty()) {
                int i = 0;

                for (Map.Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> entry : this.parse.getExceptions().entrySet()) {
                    CommandSyntaxException commandSyntaxException = (CommandSyntaxException)entry.getValue();
                    if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        i++;
                    } else {
                        this.messages.add(formatException(commandSyntaxException));
                    }
                }

                if (i > 0) {
                    this.messages.add(formatException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
                }
            } else if (this.parse.getReader().canRead()) {
                bl = true;
            }
        }

        this.x = 0;
        this.width = this.owner.width;
        if (this.messages.isEmpty() && !this.showUsages(ChatFormatting.GRAY) && bl) {
            this.messages.add(formatException(Commands.getParseException(this.parse)));
        }

        this.window = null;
        if (this.windowActive && this.client.options.autoSuggestions().get()) {
            this.show(false);
        }
    }

    private boolean showUsages(ChatFormatting formatting) {
        CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = this.parse.getContext();
        SuggestionContext<SharedSuggestionProvider> suggestionContext = commandContextBuilder.findSuggestionContext(this.textField.getCursorPosition());
        Map<CommandNode<SharedSuggestionProvider>, String> map = this.client
                .player
                .connection
                .getCommands()
                .getSmartUsage(suggestionContext.parent, this.client.player.connection.getSuggestionsProvider());
        List<FormattedCharSequence> list = Lists.<FormattedCharSequence>newArrayList();
        int i = 0;
        Style style = Style.EMPTY.withColor(formatting);

        for (Map.Entry<CommandNode<SharedSuggestionProvider>, String> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof LiteralCommandNode)) {
                list.add(FormattedCharSequence.forward(entry.getValue(), style));
                i = Math.max(i, this.textRenderer.width(entry.getValue()));
            }
        }

        if (!list.isEmpty()) {
            this.messages.addAll(list);
            this.x = Mth.clamp(this.textField.getScreenX(suggestionContext.startPos), 0, this.textField.getScreenX(0) + this.textField.getInnerWidth() - i);
            this.width = i;
            return true;
        } else {
            return false;
        }
    }

    private FormattedCharSequence provideRenderText(String original, int firstCharacterIndex) {
        if (this.parse != null) {
            return MacroCMDSuggestor.highlight(this.parse, original, firstCharacterIndex);
        }
        return FormattedCharSequence.forward(original, Style.EMPTY);
    }

    @Nullable
    static String getSuggestionSuffix(String original, String suggestion) {
        if (suggestion.startsWith(original)) {
            return suggestion.substring(original.length());
        }
        return null;
    }

    private static FormattedCharSequence highlight(ParseResults<SharedSuggestionProvider> parse, String original, int firstCharacterIndex) {
        List<FormattedCharSequence> list = Lists.<FormattedCharSequence>newArrayList();
        int i = 0;
        int j = -1;
        CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = parse.getContext().getLastChild();

        for (ParsedArgument<SharedSuggestionProvider, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            if (++j >= HIGHLIGHT_STYLES.size()) {
                j = 0;
            }

            int k = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0);
            if (k >= original.length()) {
                break;
            }

            int l = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
            if (l > 0) {
                list.add(FormattedCharSequence.forward(original.substring(i, k), INFO_STYLE));
                list.add(FormattedCharSequence.forward(original.substring(k, l), (Style)HIGHLIGHT_STYLES.get(j)));
                i = l;
            }
        }

        if (parse.getReader().canRead()) {
            int m = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0);
            if (m < original.length()) {
                int n = Math.min(m + parse.getReader().getRemainingLength(), original.length());
                list.add(FormattedCharSequence.forward(original.substring(i, m), INFO_STYLE));
                list.add(FormattedCharSequence.forward(original.substring(m, n), ERROR_STYLE));
                i = n;
            }
        }

        list.add(FormattedCharSequence.forward(original.substring(i), INFO_STYLE));
        return FormattedCharSequence.composite(list);
    }

    public void render(GuiGraphics context, int mouseX, int mouseY) {
        if (!this.tryRenderWindow(context, mouseX, mouseY)) {
            this.renderMessages(context);
        }
    }

    public boolean tryRenderWindow(GuiGraphics context, int mouseX, int mouseY) {
        if (this.window != null) {
            this.window.render(context, mouseX, mouseY);
            return true;
        }
        return false;
    }

    public void renderMessages(GuiGraphics context) {
        int i = 0;
        for (FormattedCharSequence orderedText : this.messages) {
            int j = this.suggestionsAbove
                    ? textField.getY() - 20 - i * 12
                    : textField.getY() + textField.getHeight() + 2 + i * 12;
            context.fill(this.x - 1, j, this.x + this.width + 1, j + 12, this.color);
            context.drawString(this.textRenderer, orderedText, this.x, j + 2, -1);
            ++i;
        }
    }

    public Component getNarration() {
        if (this.window != null) {
            return CommonComponents.NEW_LINE.copy().append(this.window.getNarration());
        }
        return CommonComponents.EMPTY;
    }

    public class SuggestionWindow {
        private final Rect2i area;
        private final String typedText;
        private final List<Suggestion> suggestions;
        private final int visibleSuggestionCount;
        private int inWindowIndex;
        private int selection;
        private Vec2 mouse = Vec2.ZERO;
        boolean completed;
        private int lastNarrationIndex;

        SuggestionWindow(int x, int y, int width, List<Suggestion> suggestions, boolean narrateFirstSuggestion) {
            int i = x - 1;
            int maximumVisible = MacroCMDSuggestor.this.maxSuggestionSize;
            if (!MacroCMDSuggestor.this.suggestionsAbove) {
                int availableHeight = MacroCMDSuggestor.this.owner.height - y - MacroCMDSuggestor.this.textField.getHeight() - 4;
                maximumVisible = Math.min(maximumVisible, Math.max(1, availableHeight / 12));
            }
            this.visibleSuggestionCount = Math.min(suggestions.size(), maximumVisible);
            int j = MacroCMDSuggestor.this.suggestionsAbove
                    ? y - 3 - this.visibleSuggestionCount * 12
                    : y + MacroCMDSuggestor.this.textField.getHeight() + 2;
            this.area = new Rect2i(i, j, width + 1, this.visibleSuggestionCount * 12);
            this.typedText = MacroCMDSuggestor.this.textField.getValue();
            this.lastNarrationIndex = narrateFirstSuggestion ? -1 : 0;
            this.suggestions = suggestions;
            this.select(0);
        }

        public void render(GuiGraphics context, int mouseX, int mouseY) {
            Message message;
            boolean bl4;
            int i = this.visibleSuggestionCount;
            int j = -5592406;
            boolean bl = this.inWindowIndex > 0;
            boolean bl2 = this.suggestions.size() > this.inWindowIndex + i;
            boolean bl3 = bl || bl2;
            boolean bl5 = bl4 = this.mouse.x != (float)mouseX || this.mouse.y != (float)mouseY;
            if (bl4) {
                this.mouse = new Vec2(mouseX, mouseY);
            }
            if (bl3) {
                int k;
                context.fill(this.area.getX(), this.area.getY() - 1, this.area.getX() + this.area.getWidth(), this.area.getY(), MacroCMDSuggestor.this.color);
                context.fill(this.area.getX(), this.area.getY() + this.area.getHeight(), this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight() + 1, MacroCMDSuggestor.this.color);
                if (bl) {
                    for (k = 0; k < this.area.getWidth(); ++k) {
                        if (k % 2 != 0) continue;
                        context.fill(this.area.getX() + k, this.area.getY() - 1, this.area.getX() + k + 1, this.area.getY(), -1);
                    }
                }
                if (bl2) {
                    for (k = 0; k < this.area.getWidth(); ++k) {
                        if (k % 2 != 0) continue;
                        context.fill(this.area.getX() + k, this.area.getY() + this.area.getHeight(), this.area.getX() + k + 1, this.area.getY() + this.area.getHeight() + 1, -1);
                    }
                }
            }
            boolean bl52 = false;
            for (int l = 0; l < i; ++l) {
                Suggestion suggestion = this.suggestions.get(l + this.inWindowIndex);
                context.fill(this.area.getX(), this.area.getY() + 12 * l, this.area.getX() + this.area.getWidth(), this.area.getY() + 12 * l + 12, MacroCMDSuggestor.this.color);
                if (mouseX > this.area.getX() && mouseX < this.area.getX() + this.area.getWidth() && mouseY > this.area.getY() + 12 * l && mouseY < this.area.getY() + 12 * l + 12) {
                    if (bl4) {
                        this.select(l + this.inWindowIndex);
                    }
                    bl52 = true;
                }
                context.drawString(MacroCMDSuggestor.this.textRenderer, suggestion.getText(), this.area.getX() + 1, this.area.getY() + 2 + 12 * l, l + this.inWindowIndex == this.selection ? Color.YELLOW.getRGB() : -5592406);
            }
            if (bl52 && (message = this.suggestions.get(this.selection).getTooltip()) != null) {
                MacroCMDSuggestor.this.owner.setTooltipForNextRenderPass(ComponentUtils.fromMessage(message));
            }
        }

        public boolean mouseClicked(int x, int y, int button) {
            if (!this.area.contains(x, y)) {
                return false;
            }
            int i = (y - this.area.getY()) / 12 + this.inWindowIndex;
            if (i >= 0 && i < this.suggestions.size()) {
                this.select(i);
                this.complete();
            }
            return true;
        }

        public boolean mouseScrolled(double amount) {
            int j;
            int i = (int)(MacroCMDSuggestor.this.client.mouseHandler.xpos() * (double)MacroCMDSuggestor.this.client.getWindow().getGuiScaledWidth() / (double)MacroCMDSuggestor.this.client.getWindow().getWidth());
            if (this.area.contains(i, j = (int)(MacroCMDSuggestor.this.client.mouseHandler.ypos() * (double)MacroCMDSuggestor.this.client.getWindow().getGuiScaledHeight() / (double)MacroCMDSuggestor.this.client.getWindow().getHeight()))) {
                this.inWindowIndex = Mth.clamp((int)((double)this.inWindowIndex - amount), 0, Math.max(this.suggestions.size() - this.visibleSuggestionCount, 0));
                return true;
            }
            return false;
        }


        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                this.scroll(-1);
                this.completed = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.scroll(1);
                this.completed = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                if (this.completed) {
                    this.scroll(Screen.hasShiftDown() ? -1 : 1);
                }

                this.complete();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                MacroCMDSuggestor.this.clearWindow();
                MacroCMDSuggestor.this.textField.setSuggestion((String)null);
                return true;
            } else {
                return false;
            }
        }

        public void scroll(int offset) {
            this.select(this.selection + offset);
            int i = this.inWindowIndex;
            int j = this.inWindowIndex + this.visibleSuggestionCount - 1;
            if (this.selection < i) {
                this.inWindowIndex = Mth.clamp(this.selection, 0, Math.max(this.suggestions.size() - this.visibleSuggestionCount, 0));
            } else if (this.selection > j) {
                this.inWindowIndex = Mth.clamp(this.selection - this.visibleSuggestionCount + 1, 0, Math.max(this.suggestions.size() - this.visibleSuggestionCount, 0));
            }
        }

        public void select(int index) {
            this.selection = index;
            if (this.selection < 0) {
                this.selection += this.suggestions.size();
            }
            if (this.selection >= this.suggestions.size()) {
                this.selection -= this.suggestions.size();
            }
            Suggestion suggestion = this.suggestions.get(this.selection);
            //MacroCMDSuggestor.this.textField.setSuggestion(MacroCMDSuggestor.getSuggestionSuffix(MacroCMDSuggestor.this.textField.getValue(), suggestion.apply(this.typedText)));
            if (this.lastNarrationIndex != this.selection) {
                MacroCMDSuggestor.this.client.getNarrator().sayNow(this.getNarration());
            }
        }

        public void complete() {
            Suggestion suggestion = this.suggestions.get(this.selection);
            MacroCMDSuggestor.this.completingSuggestions = true;
            MacroCMDSuggestor.this.textField.setValue(suggestion.apply(this.typedText));
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            MacroCMDSuggestor.this.textField.setCursorPosition(i);
            MacroCMDSuggestor.this.textField.setHighlightPos(i);
            this.select(this.selection);
            MacroCMDSuggestor.this.completingSuggestions = false;
            this.completed = true;
        }

        Component getNarration() {
            this.lastNarrationIndex = this.selection;
            Suggestion suggestion = this.suggestions.get(this.selection);
            Message message = suggestion.getTooltip();
            if (message != null) {
                return Component.translatable("narration.suggestion.tooltip", this.selection + 1, this.suggestions.size(), suggestion.getText(), Component.literal(message.getString()));
            }
            return Component.translatable("narration.suggestion", this.selection + 1, this.suggestions.size(), suggestion.getText());
        }
    }
}
