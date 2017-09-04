// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.jena;

import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.jena.Constants.*;
import static org.apache.juneau.jena.RdfCommonContext.*;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;
import org.apache.juneau.xml.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * Session object that lives for the duration of a single use of {@link RdfParser}.
 *
 * <p>
 * This class is NOT thread safe.  
 * It is typically discarded after one-time use although it can be reused against multiple inputs.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RdfParserSession extends ReaderParserSession {

	private final String rdfLanguage;
	private final Namespace juneauNs, juneauBpNs;
	private final Property pRoot, pValue, pType, pRdfType;
	private final Model model;
	private final boolean trimWhitespace, looseCollections;
	private final RDFReader rdfReader;
	private final Set<Resource> urisVisited = new HashSet<Resource>();
	private final RdfCollectionFormat collectionFormat;

	/**
	 * Create a new session using properties specified in the context.
	 *
	 * @param ctx
	 * 	The context creating this session object.
	 * 	The context contains all the configuration settings for this object.
	 * @param args
	 * 	Runtime session arguments.
	 */
	protected RdfParserSession(RdfParserContext ctx, ParserSessionArgs args) {
		super(ctx, args);
		ObjectMap jenaSettings = new ObjectMap();
		jenaSettings.putAll(ctx.jenaSettings);
		ObjectMap p = getProperties();
		if (p.isEmpty()) {
			this.rdfLanguage = ctx.rdfLanguage;
			this.juneauNs = ctx.juneauNs;
			this.juneauBpNs = ctx.juneauBpNs;
			this.trimWhitespace = ctx.trimWhitespace;
			this.collectionFormat = ctx.collectionFormat;
			this.looseCollections = ctx.looseCollections;
		} else {
			this.rdfLanguage = p.getString(RDF_language, ctx.rdfLanguage);
			this.juneauNs = (p.containsKey(RDF_juneauNs) ? NamespaceFactory.parseNamespace(p.get(RDF_juneauNs)) : ctx.juneauNs);
			this.juneauBpNs = (p.containsKey(RDF_juneauBpNs) ? NamespaceFactory.parseNamespace(p.get(RDF_juneauBpNs)) : ctx.juneauBpNs);
			this.trimWhitespace = p.getBoolean(RdfParserContext.RDF_trimWhitespace, ctx.trimWhitespace);
			this.collectionFormat = RdfCollectionFormat.valueOf(p.getString(RDF_collectionFormat, "DEFAULT"));
			this.looseCollections = p.getBoolean(RDF_looseCollections, ctx.looseCollections);
		}
		this.model = ModelFactory.createDefaultModel();
		addModelPrefix(juneauNs);
		addModelPrefix(juneauBpNs);
		this.pRoot = model.createProperty(juneauNs.getUri(), RDF_juneauNs_ROOT);
		this.pValue = model.createProperty(juneauNs.getUri(), RDF_juneauNs_VALUE);
		this.pType = model.createProperty(juneauBpNs.getUri(), RDF_juneauNs_TYPE);
		this.pRdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		rdfReader = model.getReader(rdfLanguage);

		// Note: NTripleReader throws an exception if you try to set any properties on it.
		if (! rdfLanguage.equals(LANG_NTRIPLE)) {
			for (Map.Entry<String,Object> e : jenaSettings.entrySet())
				rdfReader.setProperty(e.getKey(), e.getValue());
		}
	}

	@Override /* ReaderParserSession */
	protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws Exception {

		RDFReader r = rdfReader;
		r.read(model, pipe.getBufferedReader(), null);

		List<Resource> roots = getRoots(model);

		// Special case where we're parsing a loose collection of resources.
		if (looseCollections && type.isCollectionOrArray()) {
			Collection c = null;
			if (type.isArray() || type.isArgs())
				c = new ArrayList();
			else
				c = (
					type.canCreateNewInstance(getOuter()) 
					? (Collection<?>)type.newInstance(getOuter()) 
					: new ObjectList(this)
				);

			int argIndex = 0;
			for (Resource resource : roots)
				c.add(parseAnything(type.isArgs() ? type.getArg(argIndex++) : type.getElementType(), resource, 
					getOuter(), null));

			if (type.isArray() || type.isArgs())
				return (T)toArray(type, c);
			return (T)c;
		}

		if (roots.isEmpty())
			return null;
		if (roots.size() > 1)
			throw new ParseException(loc(), "Too many root nodes found in model:  {0}", roots.size());
		Resource resource = roots.get(0);

		return parseAnything(type, resource, getOuter(), null);
	}

	private final void addModelPrefix(Namespace ns) {
		model.setNsPrefix(ns.getName(), ns.getUri());
	}

	/*
	 * Decodes the specified string.
	 * If {@link RdfParserContext#RDF_trimWhitespace} is <jk>true</jk>, the resulting string is trimmed before decoding.
	 * If {@link #isTrimStrings()} is <jk>true</jk>, the resulting string is trimmed after decoding.
	 */
	private String decodeString(Object o) {
		if (o == null)
			return null;
		String s = o.toString();
		if (s.isEmpty())
			return s;
		if (trimWhitespace)
			s = s.trim();
		s = XmlUtils.decode(s, null);
		if (isTrimStrings())
			s = s.trim();
		return s;
	}
	
	/*
	 * Finds the roots in the model using either the "root" property to identify it,
	 * 	or by resorting to scanning the model for all nodes with no incoming predicates.
	 */
	private List<Resource> getRoots(Model m) {
		List<Resource> l = new LinkedList<Resource>();

		// First try to find the root using the "http://www.apache.org/juneau/root" property.
		Property root = m.createProperty(juneauNs.getUri(), RDF_juneauNs_ROOT);
		for (ResIterator i  = m.listResourcesWithProperty(root); i.hasNext();)
			l.add(i.next());

		if (! l.isEmpty())
			return l;

		// Otherwise, we need to find all resources that aren't objects.
		// We want to explicitly ignore statements where the subject
		// and object are the same node.
		Set<RDFNode> objects = new HashSet<RDFNode>();
		for (StmtIterator i = m.listStatements(); i.hasNext();) {
			Statement st = i.next();
			RDFNode subject = st.getSubject();
			RDFNode object = st.getObject();
			if (object.isResource() && ! object.equals(subject))
				objects.add(object);
		}
		for (ResIterator i = m.listSubjects(); i.hasNext();) {
			Resource r = i.next();
			if (! objects.contains(r))
				l.add(r);
		}
		return l;
	}

	private <T> BeanMap<T> parseIntoBeanMap(Resource r2, BeanMap<T> m) throws Exception {
		BeanMeta<T> bm = m.getMeta();
		RdfBeanMeta rbm = bm.getExtendedMeta(RdfBeanMeta.class);
		if (rbm.hasBeanUri() && r2.getURI() != null)
			rbm.getBeanUriProperty().set(m, null, r2.getURI());
		for (StmtIterator i = r2.listProperties(); i.hasNext();) {
			Statement st = i.next();
			Property p = st.getPredicate();
			String key = decodeString(p.getLocalName());
			BeanPropertyMeta pMeta = m.getPropertyMeta(key);
			setCurrentProperty(pMeta);
			if (pMeta != null) {
				RDFNode o = st.getObject();
				ClassMeta<?> cm = pMeta.getClassMeta();
				if (cm.isCollectionOrArray() && isMultiValuedCollections(pMeta)) {
					ClassMeta<?> et = cm.getElementType();
					Object value = parseAnything(et, o, m.getBean(false), pMeta);
					setName(et, value, key);
					pMeta.add(m, key, value);
				} else {
					Object value = parseAnything(cm, o, m.getBean(false), pMeta);
					setName(cm, value, key);
					pMeta.set(m, key, value);
				}
			} else if (! (p.equals(pRoot) || p.equals(pType))) {
				onUnknownProperty(null, key, m, -1, -1);
			}
			setCurrentProperty(null);
		}
		return m;
	}

	private boolean isMultiValuedCollections(BeanPropertyMeta pMeta) {
		if (pMeta != null && pMeta.getExtendedMeta(RdfBeanPropertyMeta.class).getCollectionFormat() != RdfCollectionFormat.DEFAULT)
			return pMeta.getExtendedMeta(RdfBeanPropertyMeta.class).getCollectionFormat() == RdfCollectionFormat.MULTI_VALUED;
		return collectionFormat == RdfCollectionFormat.MULTI_VALUED;
	}

	private <T> T parseAnything(ClassMeta<T> eType, RDFNode n, Object outer, BeanPropertyMeta pMeta) throws Exception {

		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> swap = (PojoSwap<T,Object>)eType.getPojoSwap(this);
		ClassMeta<?> sType = swap == null ? eType : swap.getSwapClassMeta(this);
		setCurrentClass(sType);

		if (! sType.canCreateNewInstance(outer)) {
			if (n.isResource()) {
				Statement st = n.asResource().getProperty(pType);
				if (st != null) {
					String c = st.getLiteral().getString();
					ClassMeta tcm = getClassMeta(c, pMeta, eType);
					if (tcm != null)
						sType = eType = tcm;
				}
			}
		}

		Object o = null;
		if (n.isResource() && n.asResource().getURI() != null && n.asResource().getURI().equals(RDF_NIL)) {
			// Do nothing.  Leave o == null.
		} else if (sType.isObject()) {
			if (n.isLiteral()) {
				o = n.asLiteral().getValue();
				if (o instanceof String) {
					o = decodeString(o);
				}
			}
			else if (n.isResource()) {
				Resource r = n.asResource();
				if (! urisVisited.add(r))
					o = r.getURI();
				else if (r.getProperty(pValue) != null) {
					o = parseAnything(object(), n.asResource().getProperty(pValue).getObject(), outer, null);
				} else if (isSeq(r)) {
					o = new ObjectList(this);
					parseIntoCollection(r.as(Seq.class), (Collection)o, sType, pMeta);
				} else if (isBag(r)) {
					o = new ObjectList(this);
					parseIntoCollection(r.as(Bag.class), (Collection)o, sType, pMeta);
				} else if (r.canAs(RDFList.class)) {
					o = new ObjectList(this);
					parseIntoCollection(r.as(RDFList.class), (Collection)o, sType, pMeta);
				} else {
					// If it has a URI and no child properties, we interpret this as an
					// external resource, and convert it to just a URL.
					String uri = r.getURI();
					if (uri != null && ! r.listProperties().hasNext()) {
						o = r.getURI();
					} else {
						ObjectMap m2 = new ObjectMap(this);
						parseIntoMap(r, m2, null, null, pMeta);
						o = cast(m2, pMeta, eType);
					}
				}
			} else {
				throw new ParseException(loc(), "Unrecognized node type ''{0}'' for object", n);
			}
		} else if (sType.isBoolean()) {
			o = convertToType(getValue(n, outer), boolean.class);
		} else if (sType.isCharSequence()) {
			o = decodeString(getValue(n, outer));
		} else if (sType.isChar()) {
			o = decodeString(getValue(n, outer)).charAt(0);
		} else if (sType.isNumber()) {
			o = parseNumber(getValue(n, outer).toString(), (Class<? extends Number>)sType.getInnerClass());
		} else if (sType.isMap()) {
			Resource r = n.asResource();
			if (! urisVisited.add(r))
				return null;
			Map m = (sType.canCreateNewInstance(outer) ? (Map)sType.newInstance(outer) : new ObjectMap(this));
			o = parseIntoMap(r, m, eType.getKeyType(), eType.getValueType(), pMeta);
		} else if (sType.isCollectionOrArray() || sType.isArgs()) {
			if (sType.isArray() || sType.isArgs())
				o = new ArrayList();
			else
				o = (sType.canCreateNewInstance(outer) ? (Collection<?>)sType.newInstance(outer) : new ObjectList(this));
			Resource r = n.asResource();
			if (! urisVisited.add(r))
				return null;
			if (isSeq(r)) {
				parseIntoCollection(r.as(Seq.class), (Collection)o, sType, pMeta);
			} else if (isBag(r)) {
				parseIntoCollection(r.as(Bag.class), (Collection)o, sType, pMeta);
			} else if (r.canAs(RDFList.class)) {
				parseIntoCollection(r.as(RDFList.class), (Collection)o, sType, pMeta);
			} else {
				throw new ParseException("Unrecognized node type ''{0}'' for collection", n);
			}
			if (sType.isArray() || sType.isArgs())
				o = toArray(sType, (Collection)o);
		} else if (sType.canCreateNewBean(outer)) {
			Resource r = n.asResource();
			if (! urisVisited.add(r))
				return null;
			BeanMap<?> bm = newBeanMap(outer, sType.getInnerClass());
			o = parseIntoBeanMap(r, bm).getBean();
		} else if (sType.isUri() && n.isResource()) {
			o = sType.newInstanceFromString(outer, decodeString(n.asResource().getURI()));
		} else if (sType.canCreateNewInstanceFromString(outer)) {
			o = sType.newInstanceFromString(outer, decodeString(getValue(n, outer)));
		} else if (sType.canCreateNewInstanceFromNumber(outer)) {
			o = sType.newInstanceFromNumber(this, outer, parseNumber(getValue(n, outer).toString(), sType.getNewInstanceFromNumberClass()));
		} else if (n.isResource()) {
			Resource r = n.asResource();
			Map m = new ObjectMap(this);
			parseIntoMap(r, m, sType.getKeyType(), sType.getValueType(), pMeta);
			if (m.containsKey(getBeanTypePropertyName(eType)))
				o = cast((ObjectMap)m, pMeta, eType);
			else
				throw new ParseException(loc(), "Class ''{0}'' could not be instantiated.  Reason: ''{1}''", sType.getInnerClass().getName(), sType.getNotABeanReason());
		} else {
			throw new ParseException("Class ''{0}'' could not be instantiated.  Reason: ''{1}''", sType.getInnerClass().getName(), sType.getNotABeanReason());
		}

		if (swap != null && o != null)
			o = swap.unswap(this, o, eType);

		if (outer != null)
			setParent(eType, o, outer);

		return (T)o;
	}

	private ObjectMap loc() {
		return getLastLocation();
	}

	private boolean isSeq(RDFNode n) {
		if (n.isResource()) {
			Statement st = n.asResource().getProperty(pRdfType);
			if (st != null)
				return RDF_SEQ.equals(st.getResource().getURI());
		}
		return false;
	}

	private boolean isBag(RDFNode n) {
		if (n.isResource()) {
			Statement st = n.asResource().getProperty(pRdfType);
			if (st != null)
				return RDF_BAG.equals(st.getResource().getURI());
		}
		return false;
	}

	private Object getValue(RDFNode n, Object outer) throws Exception {
		if (n.isLiteral())
			return n.asLiteral().getValue();
		if (n.isResource()) {
			Statement st = n.asResource().getProperty(pValue);
			if (st != null) {
				n = st.getObject();
				if (n.isLiteral())
					return n.asLiteral().getValue();
				return parseAnything(object(), st.getObject(), outer, null);
			}
		}
		throw new ParseException(loc(), "Unknown value type for node ''{0}''", n);
	}

	private <K,V> Map<K,V> parseIntoMap(Resource r, Map<K,V> m, ClassMeta<K> keyType, 
			ClassMeta<V> valueType, BeanPropertyMeta pMeta) throws Exception {
		// Add URI as "uri" to generic maps.
		if (r.getURI() != null) {
			K uri = convertAttrToType(m, "uri", keyType);
			V value = convertAttrToType(m, r.getURI(), valueType);
			m.put(uri, value);
		}
		for (StmtIterator i = r.listProperties(); i.hasNext();) {
			Statement st = i.next();
			Property p = st.getPredicate();
			String key = p.getLocalName();
			if (! (key.equals("root") && p.getURI().equals(juneauNs.getUri()))) {
				key = decodeString(key);
				RDFNode o = st.getObject();
				K key2 = convertAttrToType(m, key, keyType);
				V value = parseAnything(valueType, o, m, pMeta);
				setName(valueType, value, key);
				m.put(key2, value);
			}

		}
		return m;
	}

	private <E> Collection<E> parseIntoCollection(Container c, Collection<E> l, 
			ClassMeta<?> type, BeanPropertyMeta pMeta) throws Exception {
		int argIndex = 0;
		for (NodeIterator ni = c.iterator(); ni.hasNext();) {
			E e = (E)parseAnything(type.isArgs() ? type.getArg(argIndex++) : type.getElementType(), ni.next(), l, pMeta);
			l.add(e);
		}
		return l;
	}

	private <E> Collection<E> parseIntoCollection(RDFList list, Collection<E> l, 
			ClassMeta<?> type, BeanPropertyMeta pMeta) throws Exception {
		int argIndex = 0;
		for (ExtendedIterator<RDFNode> ni = list.iterator(); ni.hasNext();) {
			E e = (E)parseAnything(type.isArgs() ? type.getArg(argIndex++) : type.getElementType(), ni.next(), l, pMeta);
			l.add(e);
		}
		return l;
	}
}
