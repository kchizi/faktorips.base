/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistableTypeConverter;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Section to display the persistence properties of the attributes specific to an
 * {@link IPolicyCmptType}).
 * <p>
 * The properties are Attibute Name, Table Column Name, Size amongst others.
 * 
 */
public class PersistentAttributeInfo extends PersistentTypePartInfo implements IPersistentAttributeInfo {

    private String tableColumnName = ""; //$NON-NLS-1$

    private boolean tableColumnNullable = true;
    private boolean tableColumnUnique;

    private int tableColumnSize = 255;
    private int tableColumnPrecision = 16;
    private int tableColumnScale = 2;

    private String converterQualifiedClassName = ""; //$NON-NLS-1$

    private String sqlColumnDefinition = ""; //$NON-NLS-1$

    private DateTimeMapping temporalMapping = DateTimeMapping.DATE_ONLY;

    private IPolicyCmptTypeAttribute policyComponentTypeAttribute;

    public PersistentAttributeInfo(IPolicyCmptTypeAttribute policyComponentTypeAttribute, String id) {
        super(policyComponentTypeAttribute, id);
        this.policyComponentTypeAttribute = policyComponentTypeAttribute;
    }

    @Override
    public String getTableColumnName() {
        return tableColumnName;
    }

    @Override
    public boolean getTableColumnNullable() {
        return tableColumnNullable;
    }

    @Override
    public int getTableColumnPrecision() {
        return tableColumnPrecision;
    }

    @Override
    public int getTableColumnScale() {
        return tableColumnScale;
    }

    @Override
    public int getTableColumnSize() {
        return tableColumnSize;
    }

    @Override
    public boolean getTableColumnUnique() {
        return tableColumnUnique;
    }

    @Override
    public String getConverterQualifiedClassName() {
        return converterQualifiedClassName;
    }

    @Override
    public String getSqlColumnDefinition() {
        return sqlColumnDefinition;
    }

    @Override
    public void setTableColumnConverter(IPersistableTypeConverter newConverter) {
        throw new NotImplementedException();
    }

    @Override
    public void setTableColumnName(String newTableColumnName) {
        ArgumentCheck.notNull(newTableColumnName);
        String oldValue = tableColumnName;
        tableColumnName = newTableColumnName;
        valueChanged(oldValue, tableColumnName);
    }

    @Override
    public void setTableColumnNullable(boolean nullable) {
        boolean oldValue = tableColumnNullable;
        tableColumnNullable = nullable;
        valueChanged(oldValue, nullable);
    }

    @Override
    public void setTableColumnPrecision(int precision) {
        int oldValue = tableColumnPrecision;
        tableColumnPrecision = precision;
        valueChanged(oldValue, precision);
    }

    @Override
    public void setTableColumnScale(int scale) {
        int oldValue = tableColumnScale;
        tableColumnScale = scale;
        valueChanged(oldValue, scale);
    }

    @Override
    public void setTableColumnSize(int newTableColumnSize) {
        int oldValue = tableColumnSize;
        tableColumnSize = newTableColumnSize;
        valueChanged(oldValue, newTableColumnSize);
    }

    @Override
    public void setTableColumnUnique(boolean unique) {
        boolean oldValue = tableColumnUnique;
        tableColumnUnique = unique;
        valueChanged(oldValue, unique);
    }

    @Override
    public void setConverterQualifiedClassName(String converterQualifiedClassName) {
        ArgumentCheck.notNull(converterQualifiedClassName);
        String oldValue = this.converterQualifiedClassName;
        this.converterQualifiedClassName = converterQualifiedClassName;
        valueChanged(oldValue, converterQualifiedClassName);
    }

    @Override
    public void setSqlColumnDefinition(String sqlColumnDefinition) {
        ArgumentCheck.notNull(sqlColumnDefinition);
        String oldValue = this.sqlColumnDefinition;
        this.sqlColumnDefinition = sqlColumnDefinition;
        valueChanged(oldValue, sqlColumnDefinition);
    }

    @Override
    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute() {
        return policyComponentTypeAttribute;
    }

    @Override
    public boolean isPersistentAttribute() {
        AttributeType attrType = getPolicyComponentTypeAttribute().getAttributeType();
        return (attrType == AttributeType.CHANGEABLE || attrType == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
    }

    @Override
    public DateTimeMapping getTemporalMapping() {
        return temporalMapping;
    }

    @Override
    public void setTemporalMapping(DateTimeMapping temporalType) {
        DateTimeMapping oldValue = temporalMapping;
        temporalMapping = temporalType;

        valueChanged(oldValue, temporalMapping);
    }

    @Override
    protected String getXmlTag() {
        return XML_TAG;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        tableColumnName = element.getAttribute(PROPERTY_TABLE_COLUMN_NAME);
        tableColumnSize = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SIZE));
        tableColumnScale = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SCALE));
        tableColumnPrecision = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_PRECISION));
        tableColumnUnique = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_UNIQE));
        tableColumnNullable = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_NULLABLE));
        temporalMapping = DateTimeMapping.valueOf(element.getAttribute(PROPERTY_TEMPORAL_MAPPING));
        sqlColumnDefinition = element.getAttribute(PROPERTY_SQL_COLUMN_DEFINITION);
        converterQualifiedClassName = element.getAttribute(PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TABLE_COLUMN_NAME, tableColumnName);
        element.setAttribute(PROPERTY_TABLE_COLUMN_SIZE, String.valueOf(tableColumnSize));
        element.setAttribute(PROPERTY_TABLE_COLUMN_SCALE, String.valueOf(tableColumnScale));
        element.setAttribute(PROPERTY_TABLE_COLUMN_PRECISION, String.valueOf(tableColumnPrecision));
        element.setAttribute(PROPERTY_TABLE_COLUMN_UNIQE, String.valueOf(tableColumnUnique));
        element.setAttribute(PROPERTY_TABLE_COLUMN_NULLABLE, String.valueOf(tableColumnNullable));
        element.setAttribute(PROPERTY_TEMPORAL_MAPPING, String.valueOf(temporalMapping));
        element.setAttribute(PROPERTY_SQL_COLUMN_DEFINITION, sqlColumnDefinition);
        element.setAttribute(PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME, converterQualifiedClassName);
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (!StringUtils.isBlank(tableColumnName)
                && AttributeType.DERIVED_ON_THE_FLY.equals(getPolicyComponentTypeAttribute().getAttributeType())) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY,
                    Messages.PersistentAttributeInfo_msgColumnNameMustBeEmpty, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }

        validateWhitespaceInTableColumnName(msgList);
        if (!isPersistentAttribute() || isTransient()
                || !getPolicyComponentTypeAttribute().getPolicyCmptType().isPersistentEnabled()) {
            return;
        }

        if (StringUtils.isBlank(tableColumnName) && !getPolicyComponentTypeAttribute().isOverwrite()) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME,
                    Messages.PersistentAttributeInfo_msgEmptyColumnName, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }

        validateUsingPersistentOptions(msgList, ipsProject);

        /*
         * to get the max length we use the ips project which belongs to this object not the given
         * project, therefore it is not possible to overwrite this settings by using a different
         * project
         */
        int maxColumnNameLenght = getIpsProject().getReadOnlyProperties().getPersistenceOptions()
                .getMaxColumnNameLenght();
        if (StringUtils.isNotBlank(tableColumnName) && tableColumnName.length() > maxColumnNameLenght) {
            msgList.add(new Message(MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH,
                    NLS.bind(Messages.PersistentAttributeInfo_msgColumnNameLengthExceedsMaximumLength,
                            tableColumnName.length(), maxColumnNameLenght),
                    Message.ERROR, this, IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }

        super.validateThis(msgList, ipsProject);
    }

    private void validateWhitespaceInTableColumnName(MessageList msgList) {

        if (StringUtil.containsWhitespace(getTableColumnName())) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COLNAME_MUST_NOT_CONTAIN_WHITESPACE_CHARACTERS,
                    Messages.PersistentAttributeInfo_msgColumnNameMustNotContainWhitespaceCharacters, Message.ERROR,
                    this, IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }
    }

    private void validateUsingPersistentOptions(MessageList msgList, IIpsProject ipsProject) {
        IPersistenceOptions pOpt = ipsProject.getReadOnlyProperties().getPersistenceOptions();
        int minTableColumnSize = pOpt.getMinTableColumnSize();
        int maxTableColumnSize = pOpt.getMaxTableColumnSize();
        int minTableColumnPrecision = pOpt.getMinTableColumnPrecision();
        int maxTableColumnPrecision = pOpt.getMaxTableColumnPrecision();
        int minTableColumnScale = pOpt.getMinTableColumnScale();
        int maxTableColumnScale = pOpt.getMaxTableColumnScale();

        if (tableColumnSize < minTableColumnSize || tableColumnSize > maxTableColumnSize) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnSizeExceedsTheLimit,
                    new Object[] { minTableColumnSize, maxTableColumnSize });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE));
        }
        if (tableColumnPrecision < minTableColumnPrecision || tableColumnPrecision > maxTableColumnPrecision) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnPrecisionExceedsTheLimit,
                    new Object[] { minTableColumnPrecision, maxTableColumnPrecision });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION));
        }
        if (tableColumnScale < minTableColumnScale || tableColumnScale > maxTableColumnScale) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnScaleExceedsTheLimit,
                    new Object[] { minTableColumnScale, maxTableColumnScale });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE));
        }
    }
}
