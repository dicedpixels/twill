package xyz.dicedpixels;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.List;

public interface Parser {
    boolean condition(ClassOrInterfaceDeclaration clazz);
    List<FileParser.Entry> parse(ClassOrInterfaceDeclaration clazz, String className);
}
