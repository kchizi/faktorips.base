package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode

class Constants {

    def static XML_TAG_VALUE(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_VALUE"
    }

    def static XML_TAG_DATA(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_DATA"
    }

    def static XML_TAG_VALUE_SET(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_VALUE_SET"
    }

    def static XML_TAG_ATTRIBUTE_VALUE(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_ATTRIBUTE_VALUE"
    }

    def static XML_TAG_CONFIGURED_DEFAULT(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_CONFIGURED_DEFAULT"
    }

    def static XML_TAG_CONFIGURED_VALUE_SET(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_CONFIGURED_VALUE_SET"
    }

    def static XML_TAG_ALL_VALUES(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_ALL_VALUES"
    }

    def static XML_TAG_ENUM(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_ENUM"
    }

    def static XML_TAG_RANGE(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_RANGE"
    }

    def static XML_TAG_STEP(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_STEP"
    }

    def static XML_TAG_LOWER_BOUND(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_LOWER_BOUND"
    }

    def static XML_TAG_UPPER_BOUND(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_UPPER_BOUND"
    }

    def static XML_TAG_TABLE_CONTENT_NAME(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_TAG_TABLE_CONTENT_NAME"
    }

    def static XML_ATTRIBUTE_ATTRIBUTE(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_ATTRIBUTE_ATTRIBUTE"
    }

    def static XML_ATTRIBUTE_CONTAINS_NULL(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".XML_ATTRIBUTE_CONTAINS_NULL"
    }

    def static CONFIGURED_VALUE_SET_PREFIX(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".CONFIGURED_VALUE_SET_PREFIX"
    }

    def static CONFIGURED_DEFAULT_PREFIX(AbstractGeneratorModelNode it) {
        addImport(org.faktorips.runtime.internal.ValueToXmlHelper.name) + ".CONFIGURED_DEFAULT_PREFIX"
    }

}
