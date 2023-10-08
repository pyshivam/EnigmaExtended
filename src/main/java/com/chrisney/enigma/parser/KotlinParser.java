package com.chrisney.enigma.parser;

import com.chrisney.enigma.utils.SmartArrayList;
import com.chrisney.enigma.utils.TextUtils;
import com.chrisney.enigma.utils.Utils;

import java.util.ArrayList;

public class KotlinParser {

    private static final char cCurlyBracketOpen = '{';
    private static final char cCurlyBracketClose = '}';
    private static final char cParenthesisOpen = '('; 
    private static final char cParenthesisClose = ')';
    private static final char cBracketOpen = '[';
    private static final char cBracketClose = ']';
    private static final char cDoubleQuote = '"';
    private static final char cSingleQuote = '\'';
    private static final char cComma = ',';
    private static final char cSemicolon = ';';

    private static final char cSlash = '/';
    private static final char cStar = '*';
    private static final char cEscape = '\\';
    private static final char cColon = ':';

    private static final String sLineComment = "//";
    private static final String sBlockCommentStart = "/*";
    private static final String sBlockCommentEnd = "*/";

    private static final char[] charBreaks = {cCurlyBracketOpen, cCurlyBracketClose, cParenthesisOpen, 
            cParenthesisClose, cBracketOpen, cBracketClose, cDoubleQuote, cSingleQuote, cComma, cSemicolon};

    private static final String sPackage = "package";
    private static final String sImport = "import"; 
    private static final String sClass = "class";
    private static final String sInterface = "interface";
    private static final String sObject = "object";
    private static final String sCompanion = "companion";
    private static final String sFun = "fun";
    private static final String sVal = "val";
    private static final String sVar = "var";
    private static final String sAbstract = "abstract";
    private static final String sEnum = "enum";
    private static final String sSealed = "sealed";
    private static final String sData = "data";
    private static final String sInner = "inner";
    private static final String sOpen = "open"; 
    private static final String sOverride = "override";
    private static final String sPublic = "public";
    private static final String sPrivate = "private"; 
    private static final String sProtected = "protected";
    private static final String sInternal = "internal";
    private static final String sLateinit = "lateinit";
    private static final String sConst = "const";
    private static final String sConstructor = "constructor";
    private static final String sInit = "init";
    private static final String sThis = "this";
    private static final String sSuper = "super";
    private static final String sWhere = "where";
    private static final String sBy = "by";
    private static final String sDelegatesTo = "delegatesTo";
    private static final String sReturn = "return";
    private static final String sThrow = "throw";
    private static final String sTry = "try";
    private static final String sCatch = "catch";
    private static final String sFinally = "finally";
    private static final String sIf = "if";
    private static final String sElse = "else";
    private static final String sWhen = "when";
    private static final String sFor = "for"; 
    private static final String sDo = "do";
    private static final String sWhile = "while";
    private static final String sContinue = "continue";
    private static final String sBreak = "break";
    private static final String sIs = "is";
    private static final String sIn = "in";  
    private static final String sNot = "!"; 
    private static final String sNull = "null";
    private static final String sAs = "as";
    private static final String sOperator = "operator";

    private static final String[] keywords = {
            sPackage, sImport, sClass, sInterface, sObject, sCompanion, sFun, sVal, sVar, sAbstract, 
            sEnum, sSealed, sData, sInner, sOpen, sOverride, sPublic, sPrivate, sProtected, sInternal,
            sLateinit, sConst, sConstructor, sInit, sThis, sSuper, sWhere, sBy, sDelegatesTo, sReturn,
            sThrow, sTry, sCatch, sFinally, sIf, sElse, sWhen, sFor, sDo, sWhile, sContinue, sBreak,
            sIs, sIn, sNot, sNull, sAs, sOperator
    };

    private static final String tBoolean = "Boolean";
    private static final String tByte = "Byte";
    private static final String tShort = "Short";
    private static final String tInt = "Int";
    private static final String tLong = "Long";
    private static final String tFloat = "Float";
    private static final String tDouble = "Double";
    private static final String tChar = "Char";
    private static final String tString = "String";
    private static final String tAny = "Any";
    private static final String tUnit = "Unit";

    private static final String[] types = {
            tBoolean, tByte, tShort, tInt, tLong, tFloat, tDouble, tChar, tString, tAny, tUnit  
    };

    public KotlinCode parse(String sourceCode) {
        ArrayList<CodeString> strings = new ArrayList<>();
        ArrayList<CodeBlock> blocks = this.parse(sourceCode, null, strings, 0);
        return new KotlinCode(blocks, strings, sourceCode);
    }

    private ArrayList<CodeBlock> parse(String source, CodeBlock parent, ArrayList<CodeString> strings, int offset) {
        
        ArrayList<CodeBlock> blocks = new ArrayList<>();

        int counterCurlyBrackets = 0;
        int counterParenthesis = 0;
        int counterBrackets = 0;

        CodeBlock.BlockType currentBlock = CodeBlock.BlockType.Undefined;

        CodeString word = null;
        CodeString string = null;
        CodeBlock block = null;

        for (int i = 0; i < source.length(); i++) {

            Character prevChar = (i > 0) ? source.charAt(i - 1) : ' ';
            Character curChar = source.charAt(i);
            Character nextChar = (i < source.length() - 1) ? source.charAt(i + 1) : ' ';
            Character nextNoneEmptyChar = getNextNoneEmptyChar(source, i + 1);

            // Track String values and comments blocks:
            if (currentBlock == CodeBlock.BlockType.Undefined) {
                if ((curChar.equals(cDoubleQuote) || curChar.equals(cSingleQuote)) 
                        && !prevChar.equals(cEscape)) {
                    currentBlock = CodeBlock.BlockType.StringValue;
                } else if (curChar.equals(cSlash) && nextChar.equals(cSlash)) {
                    currentBlock = CodeBlock.BlockType.CommentLine;
                } else if (curChar.equals(cSlash) && nextChar.equals(cStar)) {
                    currentBlock = CodeBlock.BlockType.CommentBlock;
                }
            } else if (currentBlock == CodeBlock.BlockType.StringValue) {
                if ((curChar.equals(cDoubleQuote) || curChar.equals(cSingleQuote))  
                        && !prevChar.equals(cEscape))
                    currentBlock = CodeBlock.BlockType.Undefined;
            }

            // Brackets & parenthesis counters:
            if (currentBlock != CodeBlock.BlockType.CommentLine && currentBlock != CodeBlock.BlockType.CommentBlock) {
                if (curChar.equals(cCurlyBracketOpen)) counterCurlyBrackets++;
                if (curChar.equals(cCurlyBracketClose)) counterCurlyBrackets--;
                if (curChar.equals(cParenthesisOpen)) counterParenthesis++;
                if (curChar.equals(cParenthesisClose)) counterParenthesis--;
                if (curChar.equals(cBracketOpen)) counterBrackets++;
                if (curChar.equals(cBracketClose)) counterBrackets--;
            }

            // String value detection
            if  (parent == null && strings != null) {
                if (currentBlock == CodeBlock.BlockType.StringValue && string == null) {
                    string = new CodeString(i);
                } else if (string != null && currentBlock != CodeBlock.BlockType.StringValue) {
                    string.end = i + 1;
                    string.value = source.substring(string.start, string.end);
                    strings.add(string);
                    string = null;
                }
            }

            // Start new word:
            if (word == null && !TextUtils.inCharactersList(charBreaks, curChar)) {
                word = new CodeString(i);

            }
            if (TextUtils.isEmptyChar(curChar) || TextUtils.inCharactersList(charBreaks, curChar) ||
                    isEndBlockComment(currentBlock, curChar, prevChar)
            ) {

                // End of current word, then create word object:
                if (word != null) {
                    word.end = i;
                    word.value = source.substring(word.start, word.end);
                    if (currentBlock != CodeBlock.BlockType.StringValue
                            && currentBlock != CodeBlock.BlockType.CommentBlock
                            && currentBlock != CodeBlock.BlockType.CommentLine) {
                        word.isInstruction = isInstruction(word.value);
                        word.isType = isType(word.value);
                    }

                    // New Block detection:
                    if (block == null) {
                        block = new CodeBlock();
                        block.offset = offset;
                        if  (parent != null) {
                            block.hasParent = true;
                            block.parentType = parent.type;
                        }
                        block.start = i - word.value.length();
                    }

                    // Add word to current block:
                    if (!word.value.isEmpty()) block.words.add(word);
                }

                // Start new word:
                word = new CodeString(i);
                word.end = i;
                word.value = String.valueOf(curChar);

                // Add words to current block:
                if (block != null && !word.value.isEmpty()) block.words.add(word);
                word = null;
            }

            // Detect END of block
            if (block != null) {

                if ((
                        isEndOfCodeBlock(currentBlock, curChar, nextNoneEmptyChar) // End of Line of code, End of Class, Function, Condition, Loop...
                        || isEndBlockComment(currentBlock, curChar, prevChar) // End of Comment Block
                ) && counterCurlyBrackets == 0 && counterParenthesis == 0 && counterBrackets == 0) {

                    block.end = i + 1;
                    block.code = source.substring(block.start, block.end);

                    // Analyze sub source code:
                    if (!CodeBlock.isComment(currentBlock))
                        block.subIndexes = getSubBlockIndexes(block);

                    if (Utils.arrayNotEmpty(block.subIndexes)) {

                        for (CodePosition subIndexes : block.subIndexes) {

                            // Safe substring: important if method doesn't contains any sub code
                            // Example:
                            //      public fun onStateTransitionStart(toState: LauncherState) {}
                            String subCode = TextUtils.safeSubstring(block.code, subIndexes.start, subIndexes.end);

                            // Set block type:
                            block.type = getBlockType(block, true);
                            // Search block name and properties:
                            parseBlockProperties(block);

                            // Compute the sub block offset (chars index):
                            int subBlockOffset = block.start + block.offset + subIndexes.start;

                            // Parse the sub block:
                            if (subCode != null) {
                                ArrayList<CodeBlock> subBlocks = this.parse(subCode, block, null, subBlockOffset);
                                for (CodeBlock subBlock : subBlocks) {
                                    subBlock.innerOffset = subIndexes.start;
                                }
                                block.subBlocks.addAll(subBlocks);
                            } else {
                                // If no sub code, then remove sub indexes:
                                block.subIndexes = null;
                            }
                        }

                    } else {
                        // Set block type:
                        block.type = getBlockType(block, false);
                        // Search block name and properties:
                        parseBlockProperties(block);
                    }

                    blocks.add(block);
                    block = null;
                }
            }

            // Detect end block type:
            if (currentBlock == CodeBlock.BlockType.CommentLine) {
                if (TextUtils.isReturnChar(curChar))
                    currentBlock = CodeBlock.BlockType.Undefined;
            } else if (currentBlock == CodeBlock.BlockType.CommentBlock) {
                if (curChar.equals(cSlash) && prevChar.equals(cStar))
                    currentBlock = CodeBlock.BlockType.Undefined;
            }

        } // End for loop

        return blocks;
    }

    private SmartArrayList<CodePosition> getSubBlockIndexes(CodeBlock block) {
        // Same as JavaParser
        return indexes; 
    }

    private boolean isEndOfCodeBlock(CodeBlock.BlockType currentBlock, Character curChar, Character nextNoneEmptyChar) {
        return !CodeBlock.isComment(currentBlock)
            && currentBlock != CodeBlock.BlockType.StringValue
            && (
                    curChar.equals(cSemicolon) ||
                    (curChar.equals(cCurlyBracketClose) && !nextNoneEmptyChar.equals(cSemicolon))
            );
    }

    private boolean isEndBlockComment(CodeBlock.BlockType currentBlock, Character curChar, Character prevChar) {
        return currentBlock == CodeBlock.BlockType.CommentBlock && curChar.equals(cSlash) && prevChar.equals(cStar);
    }

    private Character getNextNoneEmptyChar(String data, int index) {
        // Same as JavaParser
        return c; 
    }

    private boolean isType(String word) {
        for (String t : types) {
            if (t.equals(word)) return true;
        }
        return false;
    }

    private boolean isInstruction(String word) {
        for (String k : keywords) {
            if (k.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    private void parseBlockProperties(CodeBlock block) {
  if (block.type == CodeBlock.BlockType.Class || block.type == CodeBlock.BlockType.Object) {
    parseClassName(block);
  } else if (block.type == CodeBlock.BlockType.Interface) {
    parseInterfaceName(block); 
  } else if (block.type == CodeBlock.BlockType.Function) {
    parseFunction(block);
  } else if (block.type == CodeBlock.BlockType.Property) {
    parseProperty(block);
  } else if (block.type == CodeBlock.BlockType.InitBlock) {
    parseInitBlock(block);
  } else if (block.type == CodeBlock.BlockType.Constructor) {
    parseConstructor(block);
  } else if (block.type == CodeBlock.BlockType.Companion) {
    parseCompanion(block);
  } else if (block.type == CodeBlock.BlockType.Package || block.type == CodeBlock.BlockType.Import) {
    parsePackageOrImportName(block);
  } 
}

private void parseClassName(CodeBlock block) {
  // Get class name
  for (CodeString word : block.words) {
    if (!word.isInstruction && !TextUtils.isEmpty(word.value.trim())) {
      block.name = word.value;
      return;
    }
  }
}

private void parseProperty(CodeBlock block) {
  String type = null;
  String name = null;
  boolean isMutable = false;
  
  for (CodeString word : block.words) {
    if (isBreakCharacter(word.value)) break;
    
    if (word.isType) {
      type = word.value;
    } else if (name == null) {
      name = word.value;
    } else if (word.value.equals(sVal)) {
      isMutable = false;
    } else if (word.value.equals(sVar)) {
      isMutable = true;
    }
  }
  
  block.name = name;
  block.variableType = type;
  block.isMutable = isMutable;
}

private void parseFunction(CodeBlock block) {
  String type = null;
  
  for (CodeString word : block.words) {
    if (isBreakCharacter(word.value)) break;
    
    if (word.isType && type == null) {
      type = word.value;
    } else if (!word.isInstruction) {
      block.name = word.value;
      block.returnType = type;
      return;
    }
  }
}

private void parseInterfaceName(CodeBlock block) {
  for (CodeString word : block.words) {
    if (isBreakCharacter(word.value)) break;
    if (!word.isInstruction && !TextUtils.isEmpty(word.value.trim())) {
      block.name = word.value;
      return;
    } 
  }
}

private void parseInitBlock(CodeBlock block) {
  block.name = "init"; 
}

private void parseConstructor(CodeBlock block) {
  block.name = "constructor";
} 

private void parseCompanion(CodeBlock block) {
  block.name = "companion";
}

private CodeBlock.BlockType getBlockType(CodeBlock block, boolean hasSubCode) {
  CodeString firstWord = getFirstNoneEmptyWord(block.words);
  if (firstWord == null) return CodeBlock.BlockType.Undefined;

  if (firstWord.value.equals(sPackage)) return CodeBlock.BlockType.Package;
  if (firstWord.value.equals(sImport)) return CodeBlock.BlockType.Import;

  if (hasSubCode) {
    if (firstWord.value.equals(sClass)) return CodeBlock.BlockType.Class;
    if (firstWord.value.equals(sInterface)) return CodeBlock.BlockType.Interface;
    if (firstWord.value.equals(sObject)) return CodeBlock.BlockType.Object;
    if (firstWord.value.equals(sCompanion)) return CodeBlock.BlockType.Companion;
    if (firstWord.value.equals(sFun)) return CodeBlock.BlockType.Function;
    if (firstWord.value.equals(sVal) || firstWord.value.equals(sVar)) return CodeBlock.BlockType.Property;
    if (firstWord.value.equals(sInit)) return CodeBlock.BlockType.InitBlock;

    return CodeBlock.BlockType.AnonymousInnerClass;

  } else {
    if (firstWord.value.equals(sConstructor)) return CodeBlock.BlockType.Constructor;
    return CodeBlock.BlockType.LineOfCode; 
  }
}

private void parsePackageOrImportName(CodeBlock block) {
  StringBuilder sb = new StringBuilder();
  for (CodeString word : block.words) {
    if (isBreakCharacter(word.value)) break;
    if (!TextUtils.isEmpty(word.value.trim())) {
      sb.append(word.value); 
    }
  }
  block.name = sb.toString();
}

}