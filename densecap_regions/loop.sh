dir="/home/imrankhurram/Downloads/train2014"
index=0
for f in "$dir"/*; do
  if [ "$index" -gt 73868 ]
  then
    th run_model.lua -input_image $f -output_vis_dir vis/data/$index
  fi
  echo $index
  index=$((index+1))
done
