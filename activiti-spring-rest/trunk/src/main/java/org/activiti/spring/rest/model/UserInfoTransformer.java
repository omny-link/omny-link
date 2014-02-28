package org.activiti.spring.rest.model;

import java.util.Iterator;
import java.util.Set;

import flexjson.ChainedSet;
import flexjson.JSONContext;
import flexjson.Path;
import flexjson.TypeContext;
import flexjson.transformer.ObjectTransformer;
import flexjson.transformer.Transformer;

public class UserInfoTransformer extends ObjectTransformer implements
		Transformer {


	@Override
	public void transform(Object object) {
		JSONContext context = getContext();
		Path path = context.getPath();
		ChainedSet visits = context.getVisits();
		TypeContext arrContext = context.writeOpenArray();

		Set<UserInfo> set = (Set<UserInfo>) object;
		for (Iterator<UserInfo> iterator = set.iterator(); iterator.hasNext();) {
			UserInfo info = iterator.next();
			context.writeOpenObject();

			context.writeName("id");
			context.writeQuoted(info.getId() == null ? "" : info.getId());
			context.writeComma();

			context.writeName("key");
			context.writeQuoted(info.getKey());
			context.writeComma();

			context.writeName("value");
			context.writeQuoted(info.getValue());
			context.writeComma();

			context.writeName("version");
			context.writeQuoted(String.valueOf(info.getVersion() == null ? ""
					: info.getVersion()));
			context.writeComma();

			// This is the extra bit...
			context.writeName(info.getKey());
			context.writeQuoted(info.getValue());

			context.writeCloseObject();
			if (iterator.hasNext()) {
				context.writeComma();
			}
		}
		context.writeCloseArray();

		// super.transform(object);
	}

}
