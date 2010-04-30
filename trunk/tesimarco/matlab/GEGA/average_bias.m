% average bias
clear all
Ts=[10 20 30 100];

for T=Ts
    load(['GMLE-bias-T' int2str(T)]);
    clear GMLE GMLE2  D
    avg_biasT(T)=sum(bias)/length(bias)-1;
end
save ('biasT','avg_biasT');