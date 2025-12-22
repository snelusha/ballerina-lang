/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * JSON serializer for BIRPackage and all its contained classes.
 *
 * @since 2.0.0
 */
public class BIRPackageJsonSerializer {

    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(Name.class, new NameAdapter())
                .registerTypeAdapter(PackageID.class, new PackageIDSerializer())
                .registerTypeAdapter(BType.class, new BTypeSerializer())
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .registerTypeAdapter(BIRNode.BIRPackage.class, new BIRPackageSerializer())
                .registerTypeAdapter(BIRNode.BIRImportModule.class, new BIRImportModuleSerializer())
                .registerTypeAdapter(BIRNode.BIRTypeDefinition.class, new BIRTypeDefinitionSerializer())
                .registerTypeAdapter(BIRNode.BIRGlobalVariableDcl.class, new BIRGlobalVariableDclSerializer())
                .registerTypeAdapter(BIRNode.BIRFunction.class, new BIRFunctionSerializer())
                .registerTypeAdapter(BIRNode.BIRAnnotation.class, new BIRAnnotationSerializer())
                .registerTypeAdapter(BIRNode.BIRConstant.class, new BIRConstantSerializer())
                .registerTypeAdapter(BIRNode.BIRServiceDeclaration.class, new BIRServiceDeclarationSerializer())
                .registerTypeAdapter(BIRNode.BIRVariableDcl.class, new BIRVariableDclSerializer())
                .registerTypeAdapter(BIRNode.BIRFunctionParameter.class, new BIRFunctionParameterSerializer())
                .registerTypeAdapter(BIRNode.BIRParameter.class, new BIRParameterSerializer())
                .registerTypeAdapter(BIRNode.BIRBasicBlock.class, new BIRBasicBlockSerializer())
                .registerTypeAdapter(BIRNode.BIRErrorEntry.class, new BIRErrorEntrySerializer())
                .registerTypeAdapter(BIRNode.BIRAnnotationAttachment.class, new BIRAnnotationAttachmentSerializer())
                .registerTypeAdapter(BIRNode.ConstValue.class, new ConstValueSerializer())
                .registerTypeAdapter(BIRNode.ChannelDetails.class, new ChannelDetailsSerializer());

        gson = builder.create();
    }

    /**
     * Serialize BIRPackage to JSON string.
     *
     * @param birPackage The BIRPackage to serialize
     * @return JSON string representation
     */
    public static String toJson(BIRNode.BIRPackage birPackage) {
        return gson.toJson(birPackage);
    }

    /**
     * Serialize BIRPackage to JSON string with custom Gson instance.
     *
     * @param birPackage The BIRPackage to serialize
     * @param customGson Custom Gson instance
     * @return JSON string representation
     */
    public static String toJson(BIRNode.BIRPackage birPackage, Gson customGson) {
        return customGson.toJson(birPackage);
    }

    /**
     * Get the configured Gson instance.
     *
     * @return Configured Gson instance
     */
    public static Gson getGson() {
        return gson;
    }

    // Type Adapters and Serializers

    // Helper methods
    private static JsonElement serializeBTypeList(java.util.List<BType> types, JsonSerializationContext context) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        if (types != null) {
            for (BType type : types) {
                array.add(context.serialize(type, BType.class));
            }
        }
        return array;
    }

    static class NameAdapter extends TypeAdapter<Name> {
        @Override
        public void write(JsonWriter out, Name value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.value);
            }
        }

        @Override
        public Name read(JsonReader in) throws IOException {
            return new Name(in.nextString());
        }
    }

    static class PackageIDSerializer implements JsonSerializer<PackageID> {
        @Override
        public JsonElement serialize(PackageID src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("orgName", src.orgName != null ? src.orgName.value : null);
            json.addProperty("name", src.name != null ? src.name.value : null);
            json.addProperty("pkgName", src.pkgName != null ? src.pkgName.value : null);
            json.addProperty("version", src.version != null ? src.version.value : null);
            json.addProperty("sourceFileName", src.sourceFileName != null ? src.sourceFileName.value : null);
            json.addProperty("sourceRoot", src.sourceRoot);
            json.addProperty("isTestPkg", src.isTestPkg);
            json.addProperty("skipTests", src.skipTests);
            return json;
        }
    }

    static class BTypeSerializer implements JsonSerializer<BType> {
        @Override
        public JsonElement serialize(BType src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("tag", src.tag);
            // Safely handle tsymbol which might have null fields
            String tsymbolStr = null;
            if (src.tsymbol != null) {
                try {
                    tsymbolStr = src.tsymbol.toString();
                } catch (NullPointerException e) {
                    // tsymbol.toString() might fail if pkgID or other fields are null
                    tsymbolStr = src.tsymbol.getClass().getSimpleName();
                }
            }
            json.addProperty("tsymbol", tsymbolStr);
            json.addProperty("flags", src.getFlags());
            json.addProperty("isNullable", src.isNullable());
            json.addProperty("class", src.getClass().getSimpleName());
            // Safely handle toString() which might also fail
            String typeStr;
            try {
                typeStr = src.toString();
            } catch (NullPointerException e) {
                typeStr = src.getClass().getSimpleName() + "@" + Integer.toHexString(src.hashCode());
            }
            json.addProperty("toString", typeStr);
            return json;
        }
    }

    static class LocationSerializer implements JsonSerializer<Location> {
        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return null;
            }
            JsonObject json = new JsonObject();
            json.addProperty("lineRange", src.lineRange().toString());
            json.addProperty("textRange", src.textRange().toString());
            return json;
        }
    }

    static class BIRPackageSerializer implements JsonSerializer<BIRNode.BIRPackage> {
        @Override
        public JsonElement serialize(BIRNode.BIRPackage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("packageID", context.serialize(src.packageID));
            json.add("importModules", context.serialize(src.importModules));
            json.add("typeDefs", context.serialize(src.typeDefs));
            json.add("globalVars", context.serialize(src.globalVars));
            json.add("importedGlobalVarsDummyVarDcls", context.serialize(src.importedGlobalVarsDummyVarDcls));
            json.add("functions", context.serialize(src.functions));
            json.add("annotations", context.serialize(src.annotations));
            json.add("constants", context.serialize(src.constants));
            json.add("serviceDecls", context.serialize(src.serviceDecls));
            json.addProperty("isListenerAvailable", src.isListenerAvailable);
            json.add("recordDefaultValueMap", context.serialize(src.recordDefaultValueMap));
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRImportModuleSerializer implements JsonSerializer<BIRNode.BIRImportModule> {
        @Override
        public JsonElement serialize(BIRNode.BIRImportModule src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("packageID", context.serialize(src.packageID));
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRTypeDefinitionSerializer implements JsonSerializer<BIRNode.BIRTypeDefinition> {
        @Override
        public JsonElement serialize(BIRNode.BIRTypeDefinition src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.add("internalName", context.serialize(src.internalName));
            json.add("attachedFuncs", context.serialize(src.attachedFuncs));
            json.addProperty("flags", src.flags);
            json.add("type", context.serialize(src.type, BType.class));
            json.addProperty("isBuiltin", src.isBuiltin);
            json.add("referencedTypes", serializeBTypeList(src.referencedTypes, context));
            json.add("referenceType", src.referenceType != null ? context.serialize(src.referenceType, BType.class) : null);
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.addProperty("index", src.index);
            json.add("pos", context.serialize(src.pos));
            json.add("markdownDocAttachment", context.serialize(src.markdownDocAttachment));
            return json;
        }
    }

    static class BIRGlobalVariableDclSerializer implements JsonSerializer<BIRNode.BIRGlobalVariableDcl> {
        @Override
        public JsonElement serialize(BIRNode.BIRGlobalVariableDcl src, Type typeOfSrc, 
                                      JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("flags", src.flags);
            json.add("pkgId", context.serialize(src.pkgId));
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.add("type", context.serialize(src.type, BType.class));
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.addProperty("metaVarName", src.metaVarName);
            json.addProperty("jvmVarName", src.jvmVarName);
            json.addProperty("kind", src.kind != null ? src.kind.toString() : null);
            json.addProperty("scope", src.scope != null ? src.scope.toString() : null);
            json.addProperty("ignoreVariable", src.ignoreVariable);
            json.addProperty("initialized", src.initialized);
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRFunctionSerializer implements JsonSerializer<BIRNode.BIRFunction> {
        @Override
        public JsonElement serialize(BIRNode.BIRFunction src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.addProperty("flags", src.flags);
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.add("type", context.serialize(src.type, BType.class));
            json.add("requiredParams", context.serialize(src.requiredParams));
            json.add("receiver", context.serialize(src.receiver));
            json.add("restParam", context.serialize(src.restParam));
            json.addProperty("argsCount", src.argsCount);
            json.add("localVars", context.serialize(src.localVars));
            json.add("returnVariable", context.serialize(src.returnVariable));
            json.add("parameters", context.serialize(src.parameters));
            json.add("basicBlocks", context.serialize(src.basicBlocks));
            json.add("errorTable", context.serialize(src.errorTable));
            json.add("workerName", context.serialize(src.workerName));
            json.add("workerChannels", context.serialize(src.workerChannels));
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.add("annotAttachmentsOnExternal", context.serialize(src.annotAttachmentsOnExternal));
            json.add("returnTypeAnnots", context.serialize(src.returnTypeAnnots));
            json.add("dependentGlobalVars", context.serialize(src.dependentGlobalVars));
            json.add("pathParams", context.serialize(src.pathParams));
            json.add("restPathParam", context.serialize(src.restPathParam));
            json.add("resourcePath", context.serialize(src.resourcePath));
            json.add("resourcePathSegmentPosList", context.serialize(src.resourcePathSegmentPosList));
            json.add("accessor", context.serialize(src.accessor));
            json.add("pathSegmentTypeList", serializeBTypeList(src.pathSegmentTypeList, context));
            json.addProperty("hasWorkers", src.hasWorkers);
            json.add("pos", context.serialize(src.pos));
            json.add("markdownDocAttachment", context.serialize(src.markdownDocAttachment));
            return json;
        }
    }

    static class BIRAnnotationSerializer implements JsonSerializer<BIRNode.BIRAnnotation> {
        @Override
        public JsonElement serialize(BIRNode.BIRAnnotation src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.addProperty("flags", src.flags);
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.add("attachPoints", context.serialize(src.attachPoints));
            json.add("annotationType", context.serialize(src.annotationType, BType.class));
            json.add("packageID", context.serialize(src.packageID));
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.add("pos", context.serialize(src.pos));
            json.add("markdownDocAttachment", context.serialize(src.markdownDocAttachment));
            return json;
        }
    }

    static class BIRConstantSerializer implements JsonSerializer<BIRNode.BIRConstant> {
        @Override
        public JsonElement serialize(BIRNode.BIRConstant src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("name", context.serialize(src.name));
            json.addProperty("flags", src.flags);
            json.add("type", context.serialize(src.type, BType.class));
            json.add("constValue", context.serialize(src.constValue));
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.add("pos", context.serialize(src.pos));
            json.add("markdownDocAttachment", context.serialize(src.markdownDocAttachment));
            return json;
        }
    }

    static class BIRServiceDeclarationSerializer implements JsonSerializer<BIRNode.BIRServiceDeclaration> {
        @Override
        public JsonElement serialize(BIRNode.BIRServiceDeclaration src, Type typeOfSrc, 
                                      JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("attachPoint", context.serialize(src.attachPoint));
            json.addProperty("attachPointLiteral", src.attachPointLiteral);
            json.add("listenerTypes", serializeBTypeList(src.listenerTypes, context));
            json.add("generatedName", context.serialize(src.generatedName));
            json.add("associatedClassName", context.serialize(src.associatedClassName));
            json.add("type", context.serialize(src.type, BType.class));
            json.addProperty("origin", src.origin != null ? src.origin.toString() : null);
            json.addProperty("flags", src.flags);
            json.add("pos", context.serialize(src.pos));
            json.add("markdownDocAttachment", context.serialize(src.markdownDocAttachment));
            return json;
        }
    }

    static class BIRVariableDclSerializer implements JsonSerializer<BIRNode.BIRVariableDcl> {
        @Override
        public JsonElement serialize(BIRNode.BIRVariableDcl src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("type", context.serialize(src.type, BType.class));
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.addProperty("metaVarName", src.metaVarName);
            json.addProperty("jvmVarName", src.jvmVarName);
            json.addProperty("kind", src.kind != null ? src.kind.toString() : null);
            json.addProperty("scope", src.scope != null ? src.scope.toString() : null);
            json.addProperty("ignoreVariable", src.ignoreVariable);
            json.addProperty("initialized", src.initialized);
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRFunctionParameterSerializer implements JsonSerializer<BIRNode.BIRFunctionParameter> {
        @Override
        public JsonElement serialize(BIRNode.BIRFunctionParameter src, Type typeOfSrc, 
                                      JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("hasDefaultExpr", src.hasDefaultExpr);
            json.addProperty("isPathParameter", src.isPathParameter);
            json.add("type", context.serialize(src.type, BType.class));
            json.add("name", context.serialize(src.name));
            json.add("originalName", context.serialize(src.originalName));
            json.addProperty("metaVarName", src.metaVarName);
            json.addProperty("jvmVarName", src.jvmVarName);
            json.addProperty("kind", src.kind != null ? src.kind.toString() : null);
            json.addProperty("scope", src.scope != null ? src.scope.toString() : null);
            json.addProperty("ignoreVariable", src.ignoreVariable);
            json.addProperty("initialized", src.initialized);
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRParameterSerializer implements JsonSerializer<BIRNode.BIRParameter> {
        @Override
        public JsonElement serialize(BIRNode.BIRParameter src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("name", context.serialize(src.name));
            json.addProperty("flags", src.flags);
            json.add("annotAttachments", context.serialize(src.annotAttachments));
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class BIRBasicBlockSerializer implements JsonSerializer<BIRNode.BIRBasicBlock> {
        @Override
        public JsonElement serialize(BIRNode.BIRBasicBlock src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("number", src.number);
            json.add("id", context.serialize(src.id));
            json.addProperty("instructionsCount", src.instructions != null ? src.instructions.size() : 0);
            json.addProperty("hasTerminator", src.terminator != null);
            // Note: Not serializing full instructions and terminator to avoid circular references
            // and keep output manageable. Can be added if needed.
            return json;
        }
    }

    static class BIRErrorEntrySerializer implements JsonSerializer<BIRNode.BIRErrorEntry> {
        @Override
        public JsonElement serialize(BIRNode.BIRErrorEntry src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("trapBB", src.trapBB != null ? src.trapBB.id.value : null);
            json.addProperty("endBB", src.endBB != null ? src.endBB.id.value : null);
            json.addProperty("targetBB", src.targetBB != null ? src.targetBB.id.value : null);
            // Not serializing errorOp to avoid circular references
            return json;
        }
    }

    static class BIRAnnotationAttachmentSerializer implements JsonSerializer<BIRNode.BIRAnnotationAttachment> {
        @Override
        public JsonElement serialize(BIRNode.BIRAnnotationAttachment src, Type typeOfSrc, 
                                      JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("annotPkgId", context.serialize(src.annotPkgId));
            json.add("annotTagRef", context.serialize(src.annotTagRef));
            if (src instanceof BIRNode.BIRConstAnnotationAttachment) {
                BIRNode.BIRConstAnnotationAttachment constAnnot = (BIRNode.BIRConstAnnotationAttachment) src;
                json.add("annotValue", context.serialize(constAnnot.annotValue));
            }
            json.add("pos", context.serialize(src.pos));
            return json;
        }
    }

    static class ConstValueSerializer implements JsonSerializer<BIRNode.ConstValue> {
        @Override
        public JsonElement serialize(BIRNode.ConstValue src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("type", context.serialize(src.type, BType.class));
            json.addProperty("value", src.value != null ? src.value.toString() : null);
            json.addProperty("valueClass", src.value != null ? src.value.getClass().getSimpleName() : null);
            return json;
        }
    }

    static class ChannelDetailsSerializer implements JsonSerializer<BIRNode.ChannelDetails> {
        @Override
        public JsonElement serialize(BIRNode.ChannelDetails src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("name", src.name);
            json.addProperty("channelInSameStrand", src.channelInSameStrand);
            json.addProperty("send", src.send);
            return json;
        }
    }
}
