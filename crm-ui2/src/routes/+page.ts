import type { PageLoad } from './$types';

export const load: PageLoad = () => {
	return {
		post: {
			title: 'Welcome to KnowProcess CRM',
			content: 'This is a placeholder home page.'
		}
	};
};