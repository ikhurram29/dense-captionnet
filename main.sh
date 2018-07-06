cd densecap_regions
th run_model.lua -input_dir input
cd ..
cd densecap_attribs
th run_model.lua -input_dir input -checkpoint densecap_attribs/checkpoint.t7
cd ..
cp densecap_regions/vis/data/results.json regions_results.json
cp densecap_attribs/vis/data/results.json attrib_results.json
java -classpath ".:lib/jackson-core-2.9.3.jar:lib/disco-2.1.jar:lib/jackson-core-asl-1.9.13.jar:lib/jackson-mapper-asl-1.9.13.jar:lib/stanford-corenlp-3.7.0.jar"  NMTDataInputGenerator
java -classpath ".:lib/jackson-core-2.9.3.jar:lib/disco-2.1.jar:lib/jackson-core-asl-1.9.13.jar:lib/jackson-mapper-asl-1.9.13.jar:lib/stanford-corenlp-3.7.0.jar"  WordsTagger
cp input.en nmt/nmt/nmt/my_infer_file.en
python -m nmt/nmt/nmt.nmt \
    --out_dir=nmt/nmt/nmt/nmt_attention_model \
    --inference_input_file=nmt/nmt/nmt/my_infer_file.en \
    --inference_output_file=nmt/nmt/nmt/output_infer
cp nmt/nmt/nmt/output_infer output_infer
java -classpath ".:lib/jackson-core-2.9.3.jar:lib/disco-2.1.jar:lib/jackson-core-asl-1.9.13.jar:lib/jackson-mapper-asl-1.9.13.jar:lib/stanford-corenlp-3.7.0.jar"  TagsReplacer



