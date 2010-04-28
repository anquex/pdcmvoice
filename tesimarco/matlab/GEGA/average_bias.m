% average bias

Ts=[10 20 30];

for T=Ts
    load(['GMLE-bias-T' int2str(T)]);
    avg_biasT(T)=sum(bias)/length(bias)-1;
end
save ('biasT','avg_biasT');