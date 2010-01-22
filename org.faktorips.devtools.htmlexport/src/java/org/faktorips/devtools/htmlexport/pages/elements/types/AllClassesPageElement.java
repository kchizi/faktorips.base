package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

public class AllClassesPageElement extends AbstractAllPageElement {
    public AllClassesPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        this(baseIpsElement, objects, ALL_FILTER);
    }

    public AllClassesPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsObjectFilter filter) {
        super(baseIpsElement, objects,filter);
        setTitle("All Classes");
    }

    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));
        
        List<PageElement> classes = createClassesList();

        addPageElements(new TextPageElement(classes.size() + " Classes"));
        
        if (classes.size() > 0) {
            addPageElements(new ListPageElement(classes));
        }
    }

    protected List<PageElement> createClassesList() {
        Collections.sort(objects, IPS_OBJECT_COMPARATOR);

        List<PageElement> items = new ArrayList<PageElement>(); 
        for (IIpsObject object : objects) {
            if (!filter.accept(object)) continue;
            PageElement link = new LinkPageElement(baseIpsElement, object, getLinkTarget(), object.getName(), true);
            items.add(link);
        }
        return items;
    }
}
