REL=1.200
NEXT=1.201-SNAPSHOT
ARTIFACT=click_framework

mvn -q versions:set -DnewVersion="$REL"
git commit -am "Release $REL"

mvn -DskipTests clean deploy

git tag -a "$ARTIFACT-$REL" -m "Release $REL"
git push origin HEAD
git push origin --tags

mvn -q versions:set -DnewVersion="$NEXT"
git commit -am "Next development iteration: $NEXT"
git push