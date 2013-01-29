package com.litecoding.smali2java.renderer;

import java.util.List;

import com.litecoding.smali2java.entity.smali.Param;
import com.litecoding.smali2java.entity.smali.SmaliEntity;
import com.litecoding.smali2java.entity.smali.SmaliMethod;
import com.litecoding.smali2java.expression.ConstExpression;
import com.litecoding.smali2java.expression.Expression;
import com.litecoding.smali2java.expression.ExpressionChain;
import com.litecoding.smali2java.expression.ExpressionChainBuilder;
import com.litecoding.smali2java.expression.FieldRefExpression;
import com.litecoding.smali2java.expression.ReturnExpression;


public class MethodRenderer {
	private boolean isEgyptianBraces = false;
	
	public static String renderObject(SmaliMethod smaliMethod) {
		return (new MethodRenderer()).render(smaliMethod);
	}
	
	public String render(SmaliMethod smaliMethod) {
		StringBuilder builder = new StringBuilder();
		switch(smaliMethod.getFlagValue(SmaliEntity.MASK_ACCESSIBILITY)) {
			case SmaliEntity.PUBLIC: {
				builder.append("public ");
				break;
			}
			case SmaliEntity.PROTECTED: {
				builder.append("protected ");
				break;
			}
			case SmaliEntity.PRIVATE: {
				builder.append("private ");
				break;
			}
			default: {
				break;
			}
		}
		
		if(smaliMethod.isFlagSet(SmaliEntity.STATIC)) {
			builder.append("static ");
		}
		
		if(smaliMethod.isFlagSet(SmaliEntity.FINAL)) {
			builder.append("final ");
		}
		
		if(smaliMethod.isFlagSet(SmaliEntity.ABSTRACT)) {
			builder.append("abstract ");
		}
		
		if(smaliMethod.isConstructor())
			builder.append(JavaRenderUtils.renderShortJavaClassName(smaliMethod.getName()));
		else {
			builder.append(JavaRenderUtils.renderShortComplexTypeDeclaration(smaliMethod.getReturnType()));
			builder.append(" ");
			builder.append(smaliMethod.getName());
		}
		
		builder.append(renderMethodProto(smaliMethod.getParams()));
			
		if(!isEgyptianBraces)
			builder.append("\n");
		builder.append("{\n");
		
		builder.append(renderExpressionChain(ExpressionChainBuilder.buildExpressionChain(smaliMethod.getCommands())));
		
		builder.append("}\n\n");
		return builder.toString();
	}

	private String renderMethodProto(List<Param> params) {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		for(int i = 0; i < params.size(); i++) {
			if(i > 0)
				builder.append(", ");
			
			Param param = params.get(i);
			if(param.getName() == null || param.getName().equals(""))
				param.setName(generateParamName(i, param.getType()));
			builder.append(JavaRenderUtils.renderShortComplexTypeDeclaration(param.getType()));
			builder.append(" ");
			builder.append(param.getName());
		}
		builder.append(")");
		return builder.toString();
	}
	
	/**
	 * Generates a name for an anonymous parameter
	 * @param i parameter index
	 * @param type
	 * @return
	 */
	private String generateParamName(int i, String type) {
		String tmp = JavaRenderUtils.renderShortComplexTypeDeclaration(type).replaceAll("\\[\\]", "Arr"); 
		StringBuilder builder = new StringBuilder();
		builder.append("a");
		builder.append(tmp.substring(0, 1).toUpperCase());
		builder.append(tmp.substring(1));
		builder.append(i);
		return builder.toString();
	}
	
	/**
	 * Generates a name for anonymous parameter
	 * @param i stack variable index
	 * @param type
	 * @return
	 */
	private String generateVarName(int i, String type) {
		String tmp = JavaRenderUtils.renderShortComplexTypeDeclaration(type).replaceAll("\\[\\]", "Arr"); 
		StringBuilder builder = new StringBuilder();
		builder.append("v");
		builder.append(tmp.substring(0, 1).toUpperCase());
		builder.append(tmp.substring(1));
		builder.append(i);
		return builder.toString();
	}

	private String renderExpressionChain(ExpressionChain chain) {	
		StringBuilder builder = new StringBuilder();
		for(Expression expression : chain.expressions) {
			if(expression instanceof ReturnExpression) {
				builder.append("return");
				Expression returnExpression = ((ReturnExpression)expression).getReturnExpression(); 
				if(returnExpression != null) {
					builder.append(" ");
					if(returnExpression instanceof ConstExpression)
						builder.append(((ConstExpression)returnExpression).getValue());
					else if(returnExpression instanceof FieldRefExpression) {
						FieldRefExpression fldRefExpr = (FieldRefExpression)returnExpression;
						if(fldRefExpr.getObject() == null)
							builder.append("this.");
						builder.append(fldRefExpr.getFieldName());
					}
				}
				builder.append(";\n");
			}
		}
		return builder.toString();
	}

}
