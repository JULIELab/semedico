#!/bin/bash
BRANCH=$1

if [ -z "$BRANCH" ]; then
	echo "Please specify the branch to switch to."
	exit 1
fi

# Switch branch for the top semedico project
echo "Main semedico project:"
git checkout $BRANCH
echo ""

# Switch branch for the modules; convention is they start with "semedico-"
for i in semedico-*; do
	echo "Submodule $i:"
	git -C $i checkout $BRANCH
	echo ""	
done
