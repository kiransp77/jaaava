/*
 * Copyright 2016 Federico Tomassetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.tomassetti.symbolsolver.javaparsermodel.declarations;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import me.tomassetti.symbolsolver.javaparser.Navigator;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.declarations.AccessLevel;
import me.tomassetti.symbolsolver.model.declarations.FieldDeclaration;
import me.tomassetti.symbolsolver.model.declarations.TypeDeclaration;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.model.usages.typesystem.ReferenceTypeImpl;
import me.tomassetti.symbolsolver.model.usages.typesystem.Type;

import java.util.Optional;

public class JavaParserFieldDeclaration implements FieldDeclaration {

    private VariableDeclarator variableDeclarator;
    private com.github.javaparser.ast.body.FieldDeclaration wrappedNode;
    private EnumConstantDeclaration enumConstantDeclaration;
    private TypeSolver typeSolver;

    public JavaParserFieldDeclaration(VariableDeclarator variableDeclarator, TypeSolver typeSolver) {
        if (typeSolver == null) {
            throw new IllegalArgumentException("typeSolver should not be null");
        }
        this.variableDeclarator = variableDeclarator;
        this.typeSolver = typeSolver;
        if (!(variableDeclarator.getParentNode().getParentNode() instanceof com.github.javaparser.ast.body.FieldDeclaration)) {
            throw new IllegalStateException(variableDeclarator.getParentNode().getClass().getCanonicalName());
        }
        this.wrappedNode = (com.github.javaparser.ast.body.FieldDeclaration) variableDeclarator.getParentNode().getParentNode();
    }

    public JavaParserFieldDeclaration(EnumConstantDeclaration enumConstantDeclaration, TypeSolver typeSolver) {
        if (typeSolver == null) {
            throw new IllegalArgumentException("typeSolver should not be null");
        }
        this.enumConstantDeclaration = enumConstantDeclaration;
        this.typeSolver = typeSolver;
    }

    @Override
    public Type getType() {
        if (enumConstantDeclaration != null) {
            com.github.javaparser.ast.body.EnumDeclaration enumDeclaration = (com.github.javaparser.ast.body.EnumDeclaration) enumConstantDeclaration.getParentNode();
            return new ReferenceTypeImpl(new JavaParserEnumDeclaration(enumDeclaration, typeSolver), typeSolver);
        } else {
            return JavaParserFacade.get(typeSolver).convert(wrappedNode.getElementType(), wrappedNode);
        }
    }

    @Override
    public String getName() {
        if (enumConstantDeclaration != null) {
            return enumConstantDeclaration.getName();
        } else {
            return variableDeclarator.getId().getName();
        }
    }

    @Override
    public boolean isStatic() {
        return wrappedNode.getModifiers().contains(Modifier.STATIC);
    }

    @Override
    public boolean isField() {
        return true;
    }

	/**
	 * Returns the JavaParser node associated with this JavaParserFieldDeclaration.
	 *
	 * @return A visitable JavaParser node wrapped by this object.
	 */
	public com.github.javaparser.ast.body.FieldDeclaration getWrappedNode()
	{
		return wrappedNode;
	}

    @Override
    public String toString() {
        return "JPField{" + getName() + "}";
    }

    @Override
    public AccessLevel accessLevel() {
        return Helper.toAccessLevel(wrappedNode.getModifiers());
    }

    @Override
    public TypeDeclaration declaringType() {
        Optional<com.github.javaparser.ast.body.TypeDeclaration> typeDeclaration = Navigator.findAncestor(wrappedNode, com.github.javaparser.ast.body.TypeDeclaration.class);
        if (typeDeclaration.isPresent()) {
            return JavaParserFacade.get(typeSolver).getTypeDeclaration(typeDeclaration.get());
        } else {
            throw new IllegalStateException();
        }
    }
}
